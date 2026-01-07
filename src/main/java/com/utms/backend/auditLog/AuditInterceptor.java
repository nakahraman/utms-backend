package com.utms.backend.auditLog;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Principal;
import java.util.Optional;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditService auditService;

    public AuditInterceptor(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String username = Optional.ofNullable(request.getUserPrincipal())
                .map(Principal::getName)
                .orElse("Anonymous");

        String role = resolveRoleFromSecurityContext().orElse("Anonymous");

        auditService.log(
                username,
                role,
                request.getMethod() + " " + request.getRequestURI(),
                "N/A",
                null,
                request.getRemoteAddr()
        );

        return true;
    }

    private Optional<String> resolveRoleFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return Optional.empty();

        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())              // e.g. ROLE_OIDB
                .filter(s -> s != null && !s.isBlank())
                .findFirst()
                .map(s -> s.startsWith("ROLE_") ? s.substring("ROLE_".length()) : s); // -> OIDB
    }
}
