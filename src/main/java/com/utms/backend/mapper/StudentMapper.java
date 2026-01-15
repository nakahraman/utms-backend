package com.utms.backend.mapper;

import com.utms.backend.model.dto.StudentProfileDto;
import com.utms.backend.model.entities.Student;
import com.utms.backend.model.entities.User;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public StudentProfileDto map(Student s) {

        User u = s.getUser();

        StudentProfileDto dto = new StudentProfileDto();

        dto.setStudentId(u.getStudent().getStudentId());
        dto.setStudentType(u.getStudent().getStudentType());
        dto.setName(u.getName());
        dto.setSurname(
                u.getName().contains(" ")
                        ? u.getName().substring(u.getName().lastIndexOf(" ") + 1)
                        : u.getName()
        );
        dto.setGpa(s.getGpa());
        dto.setSuccessRank(s.getSuccessRank());

        return dto;
    }
}
