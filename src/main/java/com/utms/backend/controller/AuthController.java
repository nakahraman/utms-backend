package com.utms.backend.controller;

import com.utms.backend.model.record.LoginRequest;
import com.utms.backend.model.record.RegisterRequest;
import com.utms.backend.model.entities.User;
import com.utms.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Login and registration APIs")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login via UBYS or local credentials")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(request.username(), request.password());
    }

    // SADECE external öğrenci buradan kayıt olur
    @PostMapping("/register-external")
    @Operation(summary = "Register external student", description = "External students register via UTMS")
    public User registerExternal(@Valid @RequestBody RegisterRequest req) {
        return authService.registerExternalStudent(req);
    }

    @PostMapping("/forgot")
    public void forgot(@RequestParam String email) {
        authService.sendResetLink(email);
    }

    @PostMapping("/reset")
    public void reset(@RequestParam String token,
                      @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
    }
}
