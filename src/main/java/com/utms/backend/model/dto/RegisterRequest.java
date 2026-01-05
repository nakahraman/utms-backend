package com.utms.backend.model.dto;

public record RegisterRequest(
        String username,
        String password
) {}
