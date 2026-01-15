package com.utms.backend.model.dto;

import com.utms.backend.model.enums.StudentType;
import lombok.Builder;
import lombok.Data;

@Data
public class StudentProfileDto {
    private Long studentId;
    private StudentType studentType;
    private String name;
    private String surname;
    private Double gpa;
    private Integer successRank;


}
