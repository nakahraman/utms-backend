package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.entities.Student;
import com.utms.backend.model.entities.User;
import com.utms.backend.model.enums.Role;
import com.utms.backend.model.enums.StudentType;
import com.utms.backend.model.enums.UserSource;
import com.utms.backend.repository.StudentRepository;
import com.utms.backend.security.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final UserService userService;

    public Long getStudentIdByUserId(Long userId) {
        return studentRepository.findByUserUserId(userId)
                .map(Student::getStudentId)
                .orElseThrow(() -> new BusinessException("STU-404", "Student not found"));
    }


    public Optional<Student> findStudentIdByUserId(Long userId) {
        return studentRepository.findByUserUserId(userId);
    }


    @Transactional
    public Student getOrCreateStudent(Long userId) {

        return studentRepository.findByUser_UserId(userId)
                .orElseGet(() -> createExternalStudent(userId));
    }

    private Student createExternalStudent(Long userId) {

        User user = userService.findUserById(userId)
                .orElseThrow(() -> new BusinessException("USR-404","User not found"));

        if (user.getUserSource() != UserSource.EXTERNAL)
            throw new BusinessException("STU-403","Internal student must exist beforehand");

        Student student = new Student();
        student.setUser(user);
        student.setStudentType(StudentType.EXTERNAL);

        return studentRepository.save(student);
    }


}
