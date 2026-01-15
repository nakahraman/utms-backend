package com.utms.backend.controller;

import com.utms.backend.model.dto.LoginResponse;
import com.utms.backend.model.record.LoginRequest;
import com.utms.backend.model.record.RegisterRequest;
import com.utms.backend.model.entities.User;
import com.utms.backend.service.AuthService;
import com.utms.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;


import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and registration APIs")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;


    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login via UBYS or local credentials")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {

       LoginResponse loginResponse = authService.login(req.username(), req.password());

        return ResponseEntity.ok(loginResponse);
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
        authService.resetPassword(token, passwordEncoder.encode(newPassword));
    }
}
