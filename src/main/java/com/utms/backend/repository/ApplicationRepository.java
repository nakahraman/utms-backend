package com.utms.backend.repository;

import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.OidbStatus;
import io.micrometer.common.KeyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStatus(ApplicationStatus status);

    @Query("""
            select a from Application a
            left join fetch a.student
            left join fetch a.department d
            left join fetch d.faculty
            where a.student.studentId = :studentId
            """)
    List<Application> findAllByStudentWithRelations(Long studentId);


    @Query("""
            select a from Application a
            left join fetch a.student
            left join fetch a.department d
            left join fetch d.faculty
            where a.appId = :id
            """)
    Optional<Application> findByIdWithRelations(Long id);

    @Query("""
            select a from Application a
            left join fetch a.student
            left join fetch a.department d
            left join fetch d.faculty
            where a.status in :statuses
            """)
    List<Application> findByStatusInWithRelations(@Param("statuses") List<OidbStatus> statuses);

    @Query("""
            select a from Application a
            join fetch a.student
            join fetch a.department
            where a.status in :statuses
            """)
    List<Application> findFinalResults(@Param("statuses") List<ApplicationStatus> statuses);

    List<Application> findByStatusInAndPublishedFalse(List<ApplicationStatus> statuses);


    @Query("""
    select a from Application a
    join fetch a.student
    join fetch a.department d
    where a.status in :statuses
      and d.deptId = :deptId
      and (:published is null or a.published = :published)
    order by a.createdDate desc
""")
    List<Application> findFinalizedResultsForView(
            @Param("deptId") Long deptId,
            @Param("statuses") List<ApplicationStatus> statuses,
            @Param("published") Boolean published
    );


    @Query("""
                select a
                from Application a
                join a.department d
                join d.faculty f
                where a.status in :statuses
                  and f.facultyId = :facultyId
            """)
    List<Application> getFacultyInbox(
            @Param("statuses") List<ApplicationStatus> statuses,
            @Param("facultyId") Long facultyId);


    Optional<Application> findByStudentStudentIdAndStatus(Long studentId, ApplicationStatus resultPublished);

    List<Application> findByStudent_StudentIdOrderByCreatedDateDesc(Long studentId);
    boolean existsByStudent_StudentIdAndDepartment_DeptId(Long studentId, Long deptId);

    Optional<Application> findTopByStudentStudentIdOrderByCreatedDateDesc(Long studentId);

    Optional<Application> findByAppIdAndStudent_StudentId(Long appId, Long studentId);

    List<Application> findByStatusIn(List<ApplicationStatus> statuses);
}
