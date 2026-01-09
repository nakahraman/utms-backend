package com.utms.backend.model.entities;

import com.utms.backend.model.enums.Decision;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "evaluations")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long evalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private Application application;

    private Long ygkMemberId;

    private Double score;

    @Column(name = "success_rank")
    private Integer rank;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision")
    private Decision decision;
}

