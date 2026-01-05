package com.utms.backend.model.dto;


public record LoginRequest(
        String username,
        String password
) {}
