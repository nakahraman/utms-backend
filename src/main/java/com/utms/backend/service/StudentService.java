package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.entities.Student;
import com.utms.backend.repository.StudentRepository;
import com.utms.backend.security.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final ApplicationService applicationService;


    public Long getStudentIdByUserId(Long userId) {
        return studentRepository.findByUserUserId(userId)
                .map(Student::getStudentId)
                .orElseThrow(() -> new BusinessException("STU-404", "Student not found"));
    }

}
