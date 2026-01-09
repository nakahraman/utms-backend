package com.utms.backend.model.entities;

import com.utms.backend.model.dto.DepartmentCriteriaDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "departments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;


    private String deptName;

    @AttributeOverrides({
            @AttributeOverride(name = "minGpa", column = @Column(name = "min_gpa")),
            @AttributeOverride(name = "maxSuccessRank", column = @Column(name = "max_success_rank")),
            @AttributeOverride(name = "requiresAllCoursesPassed", column = @Column(name = "requires_all_courses_passed")),
            @AttributeOverride(name = "requiresPortfolio", column = @Column(name = "requires_portfolio"))
    })
    @Embedded
    private DepartmentCriteria criteria;

    private Integer quota;

    @Column(name = "waitlist_quota")
    private Integer waitlistQuota;

}
