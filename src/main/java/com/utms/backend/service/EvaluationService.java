package com.utms.backend.service;

import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.repository.EvaluationRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class EvaluationService {

    private final ApplicationRepository applicationRepository;
    private final EvaluationRepository evaluationRepository;

    public EvaluationService(ApplicationRepository applicationRepository,
                             EvaluationRepository evaluationRepository) {
        this.applicationRepository = applicationRepository;
        this.evaluationRepository = evaluationRepository;
    }

    public List<Evaluation> evaluateDepartmentApplications(int quota) {

        List<Application> apps = applicationRepository.findByStatus("SentToDepartment");

        for (Application app : apps) {

            double transferScore =
                    (app.getGpa() * 0.1) + (Math.random() * 90);   // YKS yerine şimdilik dummy

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
}

