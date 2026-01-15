package com.utms.backend.model.entities;

import com.utms.backend.model.enums.StudentType;
import com.utms.backend.model.enums.UserSource;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false)
    private Department department;

    private Double gpa;

    private Double examScore; // YKS / ÖSYS  puanı

    @Enumerated(EnumType.STRING)
    private StudentType studentType;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;


    @Column(name = "success_rank")
    private Integer successRank;  // YKS / ÖSYS başarı sırası

}
