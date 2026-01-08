package com.utms.backend.service;

import com.utms.backend.model.entities.CustomUserDetails;
import com.utms.backend.model.entities.Faculty;
import com.utms.backend.model.entities.User;
import com.utms.backend.model.enums.Role;
import com.utms.backend.model.enums.UserSource;
import com.utms.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StudentService studentService;


    @Override
    public UserDetails loadUserByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        Faculty faculty = user.getFaculty();
        Long studentId = null;

        // SADECE INTERNAL (UBYS) öğrencilerde zorunlu
        if (user.getRole() == Role.STUDENT && user.getUserSource() == UserSource.UBYS) {
            studentId = studentService.getStudentIdByUserId(user.getUserId());
        }

        return new CustomUserDetails(user, faculty, studentId);
    }
}
