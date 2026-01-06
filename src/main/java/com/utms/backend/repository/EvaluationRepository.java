package com.utms.backend.repository;

import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByApplication_AppId(Long appId);

    @Query("""
            select e from Evaluation e
            left join fetch e.application a
            left join fetch a.department d
            left join fetch d.faculty
            where e.evalId = :id
            """)
    Optional<Evaluation> findByIdWithFaculty(@Param("id") Long id);


    @Query("""
            select distinct e from Evaluation e
            left join fetch e.application a
            left join fetch a.department d
            left join fetch d.faculty
            """)
    List<Evaluation> findAllWithFaculty();


    List<Evaluation> findByApplicationIn(List<Application> apps);

    void deleteByApplicationIn(List<Application> apps);
}
