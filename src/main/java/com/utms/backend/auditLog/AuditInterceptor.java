package com.utms.backend.auditLog;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.security.Principal;
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
        String role = request.isUserInRole("ROLE_OIDB") ? "OIDB" : "USER";

        auditService.log(username, role,
                request.getMethod() + " " + request.getRequestURI(),
                "N/A", null, request.getRemoteAddr());

        return true;
    }
}
