package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.mapper.EvaluationMapper;
import com.utms.backend.model.dto.EvaluationResponseDto;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.StudentType;
import com.utms.backend.repository.EvaluationRepository;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ApplicationStatusTransitionService transitionService;
    private final EvaluationMapper evaluationMapper;


    // INTERNAL öğrenci – otomatik skor
    private double calculateInternalStudentScore(Application app) {
        return (app.getGpa() * 10) + Math.random() * 50;
    }

    // EXTERNAL öğrenci – belge / transkript bazlı skor
    private double calculateExternalStudentScore(Application app) {
        return (app.getGpa() * 8) + Math.random() * 40;
    }


    public EvaluationResponseDto getEvaluationById(Long id) {
        return evaluationRepository.findByIdWithFaculty(id)
                .map(evaluationMapper::map)
                .orElseThrow(() -> new BusinessException("EVL-404", "Evaluation bulunamadı"));
    }


    public void save(Evaluation ev) {
        evaluationRepository.save(ev);
    }

    public Evaluation findEvaluationByApplicationId(Long id) {
        return evaluationRepository.findByApplication_AppId(id)
                .orElseThrow(() -> new BusinessException("EVL-404", "Evaluation bulunamadı"));
    }

    public List<EvaluationResponseDto> getAll() {
        return evaluationRepository.findAllWithFaculty()
                .stream()
                .map(evaluationMapper::map)
                .toList();
    }

    @Transactional
    public List<EvaluationResponseDto> evaluateApplications(List<Application> apps, int quota) {

        evaluationRepository.deleteByApplicationIn(apps);

        for (Application app : apps) {

            double score = app.getStudent().getStudentType() == StudentType.EXTERNAL
                    ? calculateExternalStudentScore(app)
                    : calculateInternalStudentScore(app);

            Evaluation ev = new Evaluation();
            ev.setApplication(app);
            ev.setScore(score);
            evaluationRepository.save(ev);
        }

        List<Evaluation> all = evaluationRepository.findByApplicationIn(apps);

        all.sort(
                Comparator.comparing(
                        Evaluation::getScore,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        int rank = 1;
        for (Evaluation ev : all) {
            ev.setRank(rank++);
            ev.setDecision(null);
            evaluationRepository.save(ev);

            transitionService.transition(ev.getApplication(),
                    ApplicationStatus.FACULTY_EVALUATED,
                    "Faculty evaluated application");
        }

        return all.stream().map(evaluationMapper::map).toList();
    }
}
