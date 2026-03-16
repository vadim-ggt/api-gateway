package com.innowise.apiGateway.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateUserServiceDto (
        UUID userId,
        String name,
        String surname,
        String email,
        LocalDate birthDate
) {}
