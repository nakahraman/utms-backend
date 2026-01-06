package com.utms.backend.repository;

import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.OidbStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudent_StudentId(Long studentId);

    List<Application> findByStatus(ApplicationStatus status);

    List<Application> findByStatusIn(List<ApplicationStatus> statuses);

    List<Application> findByStatusAndDepartment_Faculty_FacultyId(ApplicationStatus status, Long facultyId);

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
            where a.status = 'VALIDATED'
            """)
    List<Application> findValidatedWithRelations();

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

    boolean existsByStudent_StudentIdAndDepartment_DeptId(Long studentId, Long departmentId);

    @Query("""
                select a
                from Application a
                join a.student s
                join s.department d
                join d.faculty f
                where a.status in :statuses
                  and f.facultyId = :facultyId
            """)
    List<Application> findFacultyInbox(@Param("statuses") List<ApplicationStatus> statuses,
                                       @Param("facultyId") Long facultyId);

    @Query("""
                SELECT a FROM Application a
                JOIN FETCH a.student s
                WHERE a.status = :status
            """)
    List<Application> findPublishedResults(@Param("status") ApplicationStatus status);

    Optional<Application> findByStudentStudentIdAndStatus(Long studentId, ApplicationStatus resultPublished);

    @Query("""
                SELECT a FROM Application a
                JOIN FETCH a.student s
                WHERE a.status = :status
            """)
    List<Application> findByStatusWithStudent(@Param("status") ApplicationStatus status);

}
