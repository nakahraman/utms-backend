package com.utms.backend.externalIntegration.mock;

import com.utms.backend.model.entities.EnglishCertificate;
import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.EnglishCertType;
import org.springframework.stereotype.Service;

@Service
public class MockDocumentParserService {

    public MockTranscriptData parseTranscript(TransferDocument doc) {

        MockTranscriptData data = new MockTranscriptData();

        String name = doc.getFileName().toLowerCase();

        if (name.contains("high")) {

            data.setGpa(3.60);
            data.setCompletedSemesters(4);
            data.setHasFailedCourse(false);
            data.setHasLeaveSemester(false);

        } else if (name.contains("mid")) {

            data.setGpa(2.85);
            data.setCompletedSemesters(3);
            data.setHasFailedCourse(false);
            data.setHasLeaveSemester(false);

        } else { // low

            data.setGpa(3.60);
            data.setCompletedSemesters(4);
            data.setHasFailedCourse(false);
            data.setHasLeaveSemester(false);
        }

        // 🎯 Target semester transcript'ten türetilir
        int targetSemester;

        if (data.getCompletedSemesters() >= 4) {
            targetSemester = 5;
        } else if (data.getCompletedSemesters() >= 3) {
            targetSemester = 4;
        } else if (data.getCompletedSemesters() >= 2) {
            targetSemester = 3;
        } else {
            targetSemester = 1;
        }

        data.setTargetSemester(targetSemester);

        return data;
    }


    public MockYksData parseYks(TransferDocument doc) {

        MockYksData data = new MockYksData();

        String name = doc.getFileName().toLowerCase();

        if (name.contains("high")) {
            data.setSuccessRank(12000);
            data.setExamScore(475.75);   // 100–500 aralığında
        } else if (name.contains("mid")) {
            data.setSuccessRank(35000);
            data.setExamScore(425.40);
        } else {
            data.setSuccessRank(12000);
            data.setExamScore(475.75);
        }

        return data;
    }


    public MockEnglishCertData parseEnglishCertificate(EnglishCertificate cert) {

        MockEnglishCertData data = new MockEnglishCertData();
        String name = cert.getFileName().toLowerCase();

        if (name.contains("ielts")) {
            data.setType(EnglishCertType.IELTS);
            data.setScore(6.5);
            data.setDocumentNo("IELTS-2026-001");
        }
        else if (name.contains("toefl")) {
            data.setType(EnglishCertType.TOEFL);
            data.setScore(82.0);
            data.setDocumentNo("TOEFL-2026-002");
        }
        else {
            data.setType(EnglishCertType.YDS);
            data.setScore(75.0);
            data.setDocumentNo("YDS-2026-003");
        }

        return data;
    }

    public boolean parsePortfolio(TransferDocument doc) {

        String name = doc.getFileName().toLowerCase();

        if (name.contains("portfolio") || name.contains("portfolyo")) {
            return true;
        } else {
            return false;
        }
    }


    //RANDOMIZE
    /*
      private Random seededRandom(TransferDocument doc) {
        return new Random(doc.getFileName().hashCode());
    }

    public MockTranscriptData parseTranscript(TransferDocument doc) {

        Random r = seededRandom(doc);
        MockTranscriptData data = new MockTranscriptData();

        double gpa = 2.0 + (r.nextDouble() * 2.0); // 2.0 – 4.0
        int semesters = 1 + r.nextInt(6);          // 1 – 6
        boolean hasFailed = r.nextBoolean();
        boolean hasLeave = r.nextInt(10) < 2;      // %20 ihtimal

        data.setGpa(Math.round(gpa * 100.0) / 100.0);
        data.setCompletedSemesters(semesters);
        data.setHasFailedCourse(hasFailed);
        data.setHasLeaveSemester(hasLeave);

        return data;
    }

    public MockYksData parseYks(TransferDocument doc) {

        Random r = seededRandom(doc);
        MockYksData data = new MockYksData();

        int successRank = 1000 + r.nextInt(90000); // 1k – 90k
        int targetSemester = 1 + r.nextInt(4);    // 1 – 4

        data.setSuccessRank(successRank);
        data.setTargetSemester(targetSemester);

        return data;
    }

    public MockEnglishCertData parseEnglish(TransferDocument doc) {

        Random r = seededRandom(doc);
        MockEnglishCertData data = new MockEnglishCertData();

        boolean hasCert = r.nextBoolean();

        if (!hasCert) {
            data.setHasCertificate(false);
            data.setType(null);
            data.setScore(0);
            return data;
        }

        boolean isIelts = r.nextBoolean();

        data.setHasCertificate(true);

        if (isIelts) {
            data.setType("IELTS");
            double score = 4.0 + (r.nextDouble() * 4.0); // 4.0 – 8.0
            data.setScore(Math.round(score * 10.0) / 10.0);
        } else {
            data.setType("TOEFL");
            int score = 50 + r.nextInt(50); // 50 – 100
            data.setScore(score);
        }

        return data;
    }
     */
}
