package com.utms.backend.repository;

import com.utms.backend.model.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {


    Optional<Student> findByUserUserId(Long userId);


   Optional<Student> findByUser_UserId(Long userId);

}
