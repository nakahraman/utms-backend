package com.utms.backend.controller;

import com.utms.backend.model.dto.LoginRequest;
import com.utms.backend.model.dto.RegisterRequest;
import com.utms.backend.model.entities.User;
import com.utms.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Login and registration APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login via UBYS or local credentials")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(request.username(), request.password());
    }

    // SADECE external öğrenci buradan kayıt olur
    @PostMapping("/register-external")
    @Operation(summary = "Register external student", description = "External students register via UTMS")
    public User registerExternal(@RequestBody RegisterRequest req) {
        return authService.registerExternalStudent(req.username(), req.password());
    }
}
