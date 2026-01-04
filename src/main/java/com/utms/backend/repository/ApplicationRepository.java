package com.utms.backend.repository;

import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudent_StudentId(Long studentId);

    List<Application> findByStatus(String status);
    List<Application> findByStatusAndValidationStatus(String status, String validationStatus);

    List<Application> findByStatusIn(List<ApplicationStatus> statuses);

}
