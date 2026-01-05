package com.utms.backend.mapper;


import com.utms.backend.model.dto.EvaluationResponseDto;
import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.model.entities.Faculty;
import org.springframework.stereotype.Component;

@Component
public class EvaluationMapper {

    public EvaluationResponseDto map(Evaluation e) {

        Faculty f = null;

        if (e.getApplication() != null &&
            e.getApplication().getDepartment() != null) {

            f = e.getApplication().getDepartment().getFaculty();
        }


        return new EvaluationResponseDto(
                e.getEvalId(),
                e.getDecision(),
                null, // dateSent yok, DTO'dan çıkar ya da buraya ekle
                f != null ? f.getFacultyId() : null,
                f != null ? f.getFacultyName() : null
        );
    }
}
