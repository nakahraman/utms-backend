package com.utms.backend.model.entities;

import com.utms.backend.model.enums.StudentType;
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

    private String name;

    private String department;

    private Double gpa;

    private Double examScore;

    private String email;

    @Enumerated(EnumType.STRING)
    private StudentType studentType;

}
