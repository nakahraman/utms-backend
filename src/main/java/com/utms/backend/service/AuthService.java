package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.externalIntegration.ExternalUbysClient;
import com.utms.backend.model.entities.Student;
import com.utms.backend.model.entities.User;
import com.utms.backend.model.enums.Role;
import com.utms.backend.model.enums.UserSource;
import com.utms.backend.repository.StudentRepository;
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
    private final StudentRepository studentRepository;
    private final ExternalUbysClient externalUbysClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public String login(String username, String password) {

        // 🔐 UBYS mock authentication
        if (!externalUbysClient.authenticate(username, password)) {
            throw new BusinessException("AUTH-401", "UBYS doğrulama başarısız");
        }

        Role role = externalUbysClient.fetchRole(username);

        // 🔄 İlk girişte User oluştur
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username(username)
                                .passwordHash(passwordEncoder.encode(password))
                                .role(role)
                                .userSource(UserSource.UBYS)
                                .build()
                ));

        // 🔗 STUDENT → USER bağlama
        if (role == Role.STUDENT) {

            Student student = studentRepository.findByUsername(username)
                    .orElseThrow(() ->
                            new BusinessException("STU-404", "Student kaydı bulunamadı"));

            if (student.getUser() == null) {
                student.setUser(user);
                studentRepository.save(student);
            }
        }

        // 🎫 JWT üret
        return jwtService.generateToken(user);
    }

    @Transactional
    public User registerExternalStudent(String username, String password) {

        if (userRepository.findByUsername(username).isPresent())
            throw new BusinessException("USR-409", "Bu kullanıcı adı zaten kullanılıyor");

        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.STUDENT)
                .userSource(UserSource.EXTERNAL)
                .build();

        return userRepository.save(user);
    }
}
