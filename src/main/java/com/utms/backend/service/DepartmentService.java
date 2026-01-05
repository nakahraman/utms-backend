package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.mapper.DepartmentMapper;
import com.utms.backend.model.dto.DepartmentResponseDto;
import com.utms.backend.repository.DepartmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public List<DepartmentResponseDto> getAll() {
        return departmentRepository.findAllWithFaculty()
                .stream()
                .map(departmentMapper::map)
                .toList();
    }

    public DepartmentResponseDto getDepartmentById(Long id) {
        return departmentRepository.findByIdWithFaculty(id)
                .map(departmentMapper::map)
                .orElseThrow(() ->
                        new BusinessException("DPT-404", "Bölüm bulunamadı: " + id));
    }
}

