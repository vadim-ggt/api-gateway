package com.innowise.apiGateway.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${microservices.user-url}")
    private String userServiceUrl;
    @Value("${microservices.auth-url}")
    private String authServiceUrl;


    @Bean
    public WebClient authWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(authServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(getTimeoutClient()))
                        .build();
    }

    @Bean
    public WebClient userWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(userServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(getTimeoutClient()))
                .build();
    }


    private HttpClient getTimeoutClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));
    }
}
