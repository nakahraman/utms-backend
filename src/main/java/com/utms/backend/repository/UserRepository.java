package com.utms.backend.repository;

import com.utms.backend.model.entities.User;
import com.utms.backend.model.enums.UserSource;
import org.apache.el.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUserId(Long userId);


    Optional<User> findByUsernameAndUserSource(String username, UserSource userSource);
}
