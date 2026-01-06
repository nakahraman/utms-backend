package com.utms.backend.mapper;

import com.utms.backend.model.dto.DepartmentResponseDto;
import com.utms.backend.model.entities.Department;
import com.utms.backend.model.entities.Faculty;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentResponseDto map(Department d) {

        Faculty f = d.getFaculty();

        return new DepartmentResponseDto(
                d.getDeptId(),
                d.getDeptName(),
                d.getCriteria().toString(),
                f != null ? f.getFacultyId() : null,
                f != null ? f.getFacultyName() : null
        );
    }
}
