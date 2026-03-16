package com.innowise.apiGateway.controller;

import com.innowise.apiGateway.dto.CreateRegistrationDto;
import com.innowise.apiGateway.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/gateway")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/register")
    public Mono<ResponseEntity<Object>> register(@Valid @RequestBody CreateRegistrationDto registrationDto) {
        return registrationService.registerUser(registrationDto)
                .map(tokens -> ResponseEntity.status(HttpStatus.CREATED).body((Object) tokens))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body((Object) e.getMessage())
                ));
    }
}
