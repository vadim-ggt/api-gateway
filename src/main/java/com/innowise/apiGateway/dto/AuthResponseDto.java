package com.innowise.apiGateway.dto;

import java.util.UUID;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        UUID userId
) {}
