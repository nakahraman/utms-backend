package com.utms.backend.repository;

import com.utms.backend.model.entities.DepartmentAllowedSemester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentAllowedSemesterRepository
        extends JpaRepository<DepartmentAllowedSemester, Long> {

    List<DepartmentAllowedSemester> findByDepartmentDeptId(Long deptId);
}
