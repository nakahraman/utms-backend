package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.mapper.EvaluationMapper;
import com.utms.backend.model.dto.EvaluationResponseDto;
import com.utms.backend.model.entities.Department;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.model.enums.StudentType;
import com.utms.backend.repository.EvaluationRepository;
import com.utms.backend.security.SecurityUtil;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class EvaluationService {

    private final ApplicationService applicationService;
    private final EvaluationRepository evaluationRepository;
    private final ApplicationStatusTransitionService transitionService;
    private final EvaluationMapper evaluationMapper;

    public List<EvaluationResponseDto> evaluateDepartmentApplications(int quota) {

        Long facultyId = SecurityUtil.getCurrentUserFacultyId();
        List<Application> apps = applicationService.getDeptEvaluatedApplications(ApplicationStatus.SENT_TO_DEPARTMENT, facultyId);

        for (Application app : apps) {

            double transferScore;

            if (app.getStudent().getStudentType() == StudentType.EXTERNAL) {
                transferScore = calculateExternalStudentScore(app);
            } else {
                transferScore = calculateInternalStudentScore(app);
            }

            Evaluation ev = new Evaluation();
            ev.setApplication(app);
            ev.setScore(transferScore);
            evaluationRepository.save(ev);
        }

        List<Evaluation> all = evaluationRepository.findAll();
        all.sort(Comparator.comparing(Evaluation::getScore).reversed());

        int rank = 1;
        for (Evaluation ev : all) {
            ev.setRank(rank++);
            ev.setDecision(ev.getRank() <= quota ? "Primary" : "Waitlisted");
            evaluationRepository.save(ev);

            Application app = ev.getApplication();
            transitionService.transition(app, ApplicationStatus.DEPT_EVALUATED,
                    "External student routed to YDYO");
        }

        return all.stream()
                .map(evaluationMapper::map)
                .toList();
    }

    public Evaluation findApplicaitonByAppId(Long appId){
        return evaluationRepository.findByApplication_AppId(appId)
                .orElse(null);
    }

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

    public List<EvaluationResponseDto> getAll() {
        return evaluationRepository.findAllWithFaculty()
                .stream()
                .map(evaluationMapper::map)
                .toList();
    }
}
