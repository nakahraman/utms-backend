package com.utms.backend.model.dto;

import com.utms.backend.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Role role;
}
