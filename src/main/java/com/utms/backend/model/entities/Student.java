package com.utms.backend.model.entities;

import com.utms.backend.model.enums.StudentType;
import com.utms.backend.model.enums.UserSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "students")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    private Double gpa;

    private Double examScore;

    @Enumerated(EnumType.STRING)
    private StudentType studentType;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
