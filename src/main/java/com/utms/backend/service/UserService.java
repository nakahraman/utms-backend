package com.utms.backend.service;

import com.utms.backend.model.entities.User;
import com.utms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> findUserById(Long userId) {
        return userRepository.findByUserId(userId);
    }

}
