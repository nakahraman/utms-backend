package com.utms.backend.model.entities;

import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.ValidationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appId;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private Double gpa;
    private LocalDateTime submissionDate;

    //Başvurunun ÖİDB içerik doğruluğu

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    private ValidationStatus validationStatus;


}

