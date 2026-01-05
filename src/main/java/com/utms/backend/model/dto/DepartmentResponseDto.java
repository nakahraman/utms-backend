package com.utms.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DepartmentResponseDto {

    private Long deptId;
    private String deptName;
    private String criteria;
    private Long facultyId;
    private String facultyName;
}
