package com.utms.backend.security;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static CustomUserDetails currentUser() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated())
            throw new BusinessException("SEC-401", "Kullanıcı doğrulanamadı.");

        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetails))
            throw new BusinessException("SEC-402", "Geçersiz kullanıcı oturumu.");

        return (CustomUserDetails) principal;
    }

    public static Long getCurrentUserFacultyId() {

        CustomUserDetails u = currentUser();

        if (u.getFaculty() == null)
            throw new BusinessException("SEC-403",
                    "Bu işlem için faculty bilgisi gereklidir.");

        return u.getFaculty().getFacultyId();
    }

    public static Long getCurrentStudentId() {

        CustomUserDetails u = currentUser();

        if (u.getStudentId() == null)
            throw new BusinessException("SEC-403",
                    "Bu işlem sadece öğrenciler için geçerlidir.");

        return u.getStudentId();
    }

    public static Long getCurrentUserId() {

        return currentUser().getUserId();

    }

}
