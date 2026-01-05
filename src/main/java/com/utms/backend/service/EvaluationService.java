package com.utms.backend.service;

import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.model.enums.StudentType;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.repository.EvaluationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class EvaluationService {

    private final ApplicationRepository applicationRepository;
    private final EvaluationRepository evaluationRepository;

    public List<Evaluation> evaluateDepartmentApplications(int quota) {

        List<Application> apps =
                applicationRepository.findByStatus(ApplicationStatus.SENT_TO_DEPARTMENT);

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
            app.setStatus(ApplicationStatus.DEPT_EVALUATED);
            applicationRepository.save(app);
        }

        return all;
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
}
