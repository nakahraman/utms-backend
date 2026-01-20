package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.Department;
import com.utms.backend.model.entities.Student;
import com.utms.backend.model.entities.User;
import com.utms.backend.model.enums.StudentType;
import com.utms.backend.model.enums.UserSource;
import com.utms.backend.model.record.ExternalProfileRequest;
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
    private final DepartmentService departmentService;


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

        // EXTERNAL USER İÇİN: student yoksa YENİ KAYIT OLUŞTURMA!
        return studentRepository.findByUser_UserId(userId)
                .orElse(null);
    }


    public Student findStudentOrFail(Long userId) {

        return studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(
                        "STU-400",
                        "Başvuru yapmadan önce profil bilgilerinizi doldurmanız gerekmektedir."
                ));
    }

    public Optional<Student> findStudentIdByUserId(Long userId) {
        return studentRepository.findByUserUserId(userId);
    }


    public void save(Student student) {
        studentRepository.save(student);
    }

    public Student findStudentIdByStudentId(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("STU-404", "Student not found"));
    }

    public boolean isExternalProfileComplete(Long userId) {

        Student student = studentRepository.findByUser_UserId(userId)
                .orElse(null);

        if (student == null)
            return false;

        if (student.getStudentType() == StudentType.INTERNAL)
            return true;

        return student.getDepartment() != null
               && student.getGpa() != null
               && student.getExamScore() != null;
    }


    @Transactional
    public void updateExternalProfile(Long userId, ExternalProfileRequest req) {

        User user = userService.findUserById(userId)
                .orElseThrow(() -> new BusinessException("USR-404", "Kullanıcı bulunamadı"));

        if (user.getUserSource() != UserSource.EXTERNAL) {
            throw new BusinessException("STU-400",
                    "Sadece external kullanıcılar profil güncelleyebilir");
        }

        Department dept = departmentService.findDepartmentById(req.departmentId());

        Student student = studentRepository.findByUser_UserId(userId)
                .orElse(null);

        // Eğer daha önce boş student kaydı oluşturulmamışsa
        if (student == null) {
            student = new Student();
            student.setUser(user);
            student.setStudentType(StudentType.EXTERNAL);
            student.setDepartment(dept);
            student.setGpa(req.gpa());
            student.setExamScore(req.examScore());
        }


        student.setDepartment(dept);
        student.setGpa(req.gpa());
        student.setExamScore(req.examScore());

        studentRepository.save(student);
    }

}
