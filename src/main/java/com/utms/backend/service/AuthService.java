package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.externalIntegration.ExternalUbysClient;
import com.utms.backend.model.entities.User;
import com.utms.backend.model.enums.Role;
import com.utms.backend.model.enums.StudentType;
import com.utms.backend.model.enums.UserSource;
import com.utms.backend.repository.UserRepository;

import com.utms.backend.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ExternalUbysClient externalUbysClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(String username, String password) {

        var userOpt = userRepository.findByUsername(username);

        // 🔄 UBYS’den ilk defa gelen kullanıcı otomatik oluşturulur
        if (userOpt.isEmpty() && externalUbysClient.authenticate(username, password)) {

            User u = new User();
            u.setUsername(username);
            u.setPasswordHash(passwordEncoder.encode(password));
            u.setRole(externalUbysClient.fetchRole(username));   // mock role
            u.setUserSource(UserSource.UBYS);

            userRepository.save(u);
            userOpt = java.util.Optional.of(u);
        }


        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USR-404", "Kullanıcı bulunamadı"));

        // 🔐 UBYS kullanıcıları
        if (user.getUserSource() == UserSource.UBYS) {

            // Mock UBYS doğrulaması
            if (!externalUbysClient.authenticate(username, password)) {
                throw new BusinessException("AUTH-401", "UBYS doğrulama başarısız");
            }

        }
        // 🔐 External / LOCAL kullanıcılar
        else {

            if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(password, user.getPasswordHash())) {

                throw new BusinessException("AUTH-401", "Hatalı parola");
            }
        }

        // 🎫 JWT üretimi
        return jwtService.generateToken(user);
    }


    public User registerExternalStudent(String username, String password) {

        if (userRepository.findByUsername(username).isPresent())
            throw new BusinessException("USR-409", "Bu kullanıcı adı zaten kullanılıyor");

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(Role.STUDENT);
        user.setUserSource(UserSource.EXTERNAL);

        return userRepository.save(user);
    }
}
