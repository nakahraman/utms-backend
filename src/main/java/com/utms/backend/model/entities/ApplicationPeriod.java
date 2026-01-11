package com.utms.backend.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_periods")
@Data
public class ApplicationPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime startsAt;
    private LocalDateTime endsAt;

    private boolean active;

    public ApplicationPeriod() {
    }
}
