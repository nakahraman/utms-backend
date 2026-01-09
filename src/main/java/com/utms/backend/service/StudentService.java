package com.utms.backend.service;

import com.utms.backend.eligibility.externalStudent.ExternalEligibilityExtractor;
import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.AcademicEligibilitySnapshot;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Student;
import com.utms.backend.model.entities.User;
import com.utms.backend.model.enums.StudentType;
import com.utms.backend.model.enums.UserSource;
import com.utms.backend.repository.StudentRepository;
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

    @Transactional
    public Student resolveStudent(Long userId) {

        User user = userService.findUserById(userId)
                .orElseThrow(() -> new BusinessException("USR-404", "Kullanıcı bulunamadı"));

        if (user.getUserSource() == UserSource.UBYS) {
            return studentRepository.findByUser_UserId(userId)
                    .orElseThrow(() ->
                            new BusinessException("STU-404",
                                    "UBYS kullanıcısına ait öğrenci bulunamadı"));
        }

        return studentRepository.findByUser_UserId(userId)
                .orElseGet(() -> {

                    Student s = new Student();
                    s.setUser(user);
                    s.setStudentType(StudentType.EXTERNAL);
                    return studentRepository.save(s);
                });
    }

    public Optional<Student> findStudentIdByUserId(Long userId) {
        return studentRepository.findByUserUserId(userId);
    }

    public void save(Student student) {
        studentRepository.save(student);
    }
}
