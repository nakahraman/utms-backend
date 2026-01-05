package com.utms.backend.model.record;


public record LoginRequest(
        String username,
        String password
) {}
