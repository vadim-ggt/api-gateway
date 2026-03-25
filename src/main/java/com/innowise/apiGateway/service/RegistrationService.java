package com.innowise.apiGateway.service;

import com.innowise.apiGateway.dto.AuthResponseDto;
import com.innowise.apiGateway.dto.CreateAuthServiceDto;
import com.innowise.apiGateway.dto.CreateRegistrationDto;
import com.innowise.apiGateway.dto.CreateUserServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationService {

    private final WebClient authWebClient;
    private final WebClient userWebClient;

    @Value("${internal.api-key}")
    private String internalApiKey;

    public Mono<AuthResponseDto> registerUser(CreateRegistrationDto request) {
        log.info("SAGA : Registering user with email: {}",request.email());

        CreateAuthServiceDto authRequest = new CreateAuthServiceDto(request.email(),
                                                                    request.password());

        return authWebClient.post()
                .uri("/api/auth/register")
                .bodyValue(authRequest)
                .retrieve()
                .bodyToMono(AuthResponseDto.class)
                .flatMap(authResponse -> {
                    UUID userId = authResponse.userId();
                    log.info("Auth step SUCCESS. UserId : {}", userId);

                    return createProfileInUserService(userId, request)
                            .onErrorResume(e -> {
                                log.error("User step FAILED. UserId : {}", userId);

                                return rollbackAuthService(userId)
                                        .then(Mono.error(new RuntimeException("Registration failed at profiles step:  " + e.getMessage())));
                            })
                            .thenReturn(authResponse);
                });
    }

    private Mono<Void> createProfileInUserService(UUID userId, CreateRegistrationDto request) {
        CreateUserServiceDto userRequest = new CreateUserServiceDto(
                userId,
                request.name(),
                request.surname(),
                request.email(),
                request.birthDate());

        return userWebClient.post()
                .uri("/api/users")
                .header("x-internal-key", internalApiKey)
                .bodyValue(userRequest)
                .retrieve()
                .bodyToMono(Void.class);
    }


    private Mono<Void> rollbackAuthService(UUID userId) {
        return authWebClient.delete()
                .uri("/api/auth/internal/user/" + userId)
                .header("x-internal-key", internalApiKey)
                .retrieve()
                .bodyToMono(Void.class)

                //личная вича механизма RETRY
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .doBeforeRetry(rs -> log.warn("Retrying rollback for user {}. Attempt {}", userId, rs.totalRetries()+1)))
                .doOnSuccess(v -> log.info("Rollback for user {} success", userId))
                .doOnError(e -> log.error("Rollback for user {} failed", userId));

    }
}
