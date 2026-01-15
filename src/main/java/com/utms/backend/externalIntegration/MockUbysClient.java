package com.utms.backend.externalIntegration;


import com.utms.backend.model.enums.UserSource;
import com.utms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class MockUbysClient implements ExternalUbysClient {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /*
    @Override
    public boolean authenticate(String username, String password) {

        return userRepository.findByUsernameAndUserSource(username, UserSource.UBYS)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .isPresent();
    }

     */

    @Override
    public boolean authenticate(String username, String password) {

        // Gerçek UBYS yerine basit mock kuralı
        return username.startsWith("std") && password.equals("123456");
    }
}

