package com.utms.backend.controller;

import com.utms.backend.model.dto.DepartmentResponseDto;
import com.utms.backend.service.DepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@AllArgsConstructor
@Tag(name = "Departments", description = "Department listing APIs")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public List<DepartmentResponseDto> getAll() {
        return departmentService.getAll();
    }

    @GetMapping("/{id}")
    public DepartmentResponseDto getById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }
}
