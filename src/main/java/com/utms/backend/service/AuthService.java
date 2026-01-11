package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.externalIntegration.ExternalUbysClient;
import com.utms.backend.model.entities.PasswordResetToken;
import com.utms.backend.model.entities.User;
import com.utms.backend.model.enums.Role;
import com.utms.backend.model.enums.UserSource;
import com.utms.backend.model.record.RegisterRequest;
import com.utms.backend.repository.PasswordResetTokenRepository;
import com.utms.backend.repository.UserRepository;
import com.utms.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentService studentService;
    private final ExternalUbysClient externalUbysClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    public String login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("AUTH-404", "Kullanıcı bulunamadı"));

        // 🔴 1. ADIM — HESAP KİLİTLİ Mİ?
        validateAccountStatus(user);

        boolean authenticated = false;

        if (user.getUserSource() == UserSource.UBYS) {   // INTERNAL kullanıcı

            authenticated = externalUbysClient.authenticate(username, password);

        } else {   // EXTERNAL kullanıcı

            authenticated = passwordEncoder.matches(password, user.getPasswordHash());
        }

        // 🔴 2. ADIM — BAŞARISIZ GİRİŞ
        if (!authenticated) {
            handleFailedLogin(user);
            throw new BusinessException("AUTH-401", "Geçersiz kullanıcı adı veya şifre");
        }

        // 🔴 3. ADIM — BAŞARILI GİRİŞ RESET
        resetLoginFailures(user);

        // 🔗 STUDENT → USER ilişkisinin DB'de varlığını garanti et
        if (user.getRole() == Role.STUDENT
            && user.getUserSource() == UserSource.UBYS) {

            studentService.findStudentIdByUserId(user.getUserId())
                    .orElseThrow(() -> new BusinessException(
                            "STU-500",
                            "Bu UBYS öğrencisine ait sistemde Student kaydı bulunmuyor. "
                            + "OIDB tarafından kayıt oluşturulmalıdır."
                    ));
        }

        return jwtService.generateToken(user);
    }

    private void validateAccountStatus(User user) {

        if (user.getAccountLockedUntil() != null &&
            user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {

            throw new BusinessException("AUTH-423",
                    "Account locked. Please contact Student Affairs IT Support.");
        }
    }

    private void handleFailedLogin(User user) {

        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= 3) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
        }

        userRepository.save(user);
    }

    private void resetLoginFailures(User user) {
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);
    }

    @Transactional
    public User registerExternalStudent(RegisterRequest req) {

        if (userRepository.findByUsername(req.username()).isPresent())
            throw new BusinessException("USR-409", "Bu kullanıcı adı zaten kullanılıyor");

        User user = User.builder()
                .username(req.username())
                .passwordHash(passwordEncoder.encode(req.password()))
                .email(req.email())
                .name(req.name())
                .role(Role.STUDENT)
                .userSource(UserSource.EXTERNAL)
                .build();

        return userRepository.save(user);   //  önce User persist
    }

    public void sendResetLink(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("USR-404", "User not found"));

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        String link = "http://localhost:3000/reset-password?token=" + token;

        emailService.sendSafe(user.getEmail(),
                "UTMS Password Reset",
                "Şifrenizi sıfırlamak için linke tıklayın:\n" + link);
    }


    @Transactional
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken prt = tokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new BusinessException("PRT-404", "Invalid or expired token"));

        if (prt.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new BusinessException("PRT-410", "Token expired");

        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        // 🔓 HESAP KİLİDİNİ AÇ
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);

        userRepository.save(user);

        prt.setUsed(true);
    }


}
