package com.utms.backend.statusHistory;

import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_status_history")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hangi başvuru
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private Application application;

    // Önceki / yeni durum
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false)
    private ApplicationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private ApplicationStatus toStatus;

    // Kim değiştirdi (username veya sistem)
    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    // Ne zaman
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    // Opsiyonel açıklama
    @Column(name = "reason", length = 500)
    private String reason;
}
