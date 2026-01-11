package com.utms.backend.model.entities;

import com.utms.backend.model.enums.Role;
import com.utms.backend.model.enums.UserSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(unique = true, nullable = false)
    private String username; //

    @Column(nullable = false)
    private String passwordHash;

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserSource userSource; // UBYS or EXTERNAL

    @Column(unique = true, nullable = false)
    private String email; //

    @Column(nullable = false)
    private String name;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Student student;

    private Integer failedLoginAttempts = 0;
    private LocalDateTime accountLockedUntil;


}
