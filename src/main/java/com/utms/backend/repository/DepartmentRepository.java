package com.utms.backend.repository;


import com.utms.backend.model.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query("""
           select d from Department d
           left join fetch d.faculty
           where d.deptId = :id
           """)
    Optional<Department> findByIdWithFaculty(@Param("id") Long id);

        @Query("""
           select d from Department d
           left join fetch d.faculty
           """)
        List<Department> findAllWithFaculty();

}
