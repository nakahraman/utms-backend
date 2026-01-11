package com.utms.backend.model.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "department_allowed_semesters")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentAllowedSemester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false)
    private Department department;

    @Column(nullable = false)
    private Integer semester;
}
