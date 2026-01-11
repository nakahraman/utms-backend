package com.utms.backend.model.entities;

import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.Decision;
import com.utms.backend.model.enums.EnglishProficiencyResult;
import com.utms.backend.model.enums.ValidationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "applications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_target_dept",
                        columnNames = {"student_id", "dept_id"}
                )
        }
)
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
    @JoinColumn(name = "dept_id")
    private Department department;

    private Double gpa;
    private LocalDateTime submissionDate;

    //Başvurunun ÖİDB içerik doğruluğu

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    private ValidationStatus validationStatus;

    @Enumerated(EnumType.STRING)
    private Decision decision;

    @Column(name = "is_published")
    private boolean published;

    @Enumerated(EnumType.STRING)
    @Column(name = "english_result")
    private EnglishProficiencyResult englishResult;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evaluation> evaluations = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransferDocument> transferDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnglishCertificate> englishCertificates = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    public void onCreate() {
        this.createdDate = LocalDateTime.now();
    }


}

