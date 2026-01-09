package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.externalIntegration.ExternalUbysClient;
import com.utms.backend.model.entities.User;
import com.utms.backend.model.enums.Role;
import com.utms.backend.model.enums.UserSource;
import com.utms.backend.model.record.RegisterRequest;
import com.utms.backend.repository.UserRepository;
import com.utms.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentService studentService;
    private final ExternalUbysClient externalUbysClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("AUTH-404", "Kullanıcı bulunamadı"));

        if (user.getUserSource() == UserSource.UBYS) { // INTERNAL kullanıcı

            if (!externalUbysClient.authenticate(username, password)) {
                throw new BusinessException("AUTH-401", "UBYS doğrulama başarısız");
            }

        } else { // EXTERNAL kullanıcı

            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new BusinessException("AUTH-401", "Şifre hatalı");
            }
        }

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
}
