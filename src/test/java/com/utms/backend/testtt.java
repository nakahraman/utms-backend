package com.utms.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.record.ApplicationSubmitRequest;
import com.utms.backend.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class testtt {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Long appId = 1L;

    @Test
    void utmsStateFlow() throws Exception {

        submitApplication();

        //oidb
        getOidbInbox();
        sendToYdyo();

        //ydyo
        ydyoInbox();
        ydyoValidate();
        ydyoPlacementExam();

        //oidb
        getOidbInbox();
        validateAndSendToFaculty();

        //faculty
        facultyInbox();
        facultyReturnToOidb();

        //oidb
        getOidbInbox();
        resendToFaculty();

        //faculty
        facultyInbox();
        facultyReturnToOidb();

        //oidb
        getOidbInbox();
        resendToFaculty();

        //faculty
        facultyInbox();
        facultyEvaluate();

        //oidb
        getOidbInbox();
        sendToYgk();

        //ygk
        ygkInbox();
        ygkFinalize();

        //oidb
        getOidbInbox();
        publishResults();

        //student
        getMyResult();
    }


    private void resendToFaculty() throws Exception {

        mockMvc.perform(post("/oidb/resend-to-faculty")
                        .with(user("oidb1").roles("OIDB"))
                        .param("appId", "1"))
                .andExpect(status().isOk());
    }
    private void facultyEvaluate() throws Exception {

        mockMvc.perform(post("/faculty/evaluate")
                        .with(user("faculty1").roles("FACULTY"))
                        .param("quota", "5"))
                .andExpect(status().isOk());
    }

    private void facultyReturnToOidb() throws Exception {

        mockMvc.perform(post("/faculty/return")
                        .with(user("faculty1").roles("FACULTY"))
                        .param("appId", "1")
                        .param("reason", "Eksik belge"))
                .andExpect(status().isOk());
    }
    private void facultyInbox() throws Exception {

        mockMvc.perform(get("/faculty/inbox")
                        .with(user("faculty1").roles("FACULTY")))
                .andExpect(status().isOk());
    }
    private void submitApplication() throws Exception {

        ApplicationSubmitRequest req = new ApplicationSubmitRequest(1L);

        mockMvc.perform(post("/student/submit")
                        .with(user("std1").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());


    }

    private void getMyResult() throws Exception {

        mockMvc.perform(get("/student/student/results")
                        .with(user("std1").roles("STUDENT")))
                .andExpect(status().isOk());
    }
    private void publishResults() throws Exception {

        mockMvc.perform(post("/oidb/publish-results")
                        .with(user("oidb1").roles("OIDB")))
                .andExpect(status().isOk());
    }

    private void getOidbInbox() throws Exception {

        mockMvc.perform(get("/oidb/inbox")
                        .with(user("oidb1").roles("OIDB")))
                .andExpect(status().isOk());


    }

    private void sendToYdyo() throws Exception {

        mockMvc.perform(post("/oidb/send-to-ydyo")
                        .with(user("oidb1").roles("OIDB"))
                        .param("appId", appId.toString()))
                .andExpect(status().isOk());


    }

    private void ydyoInbox() throws Exception {

        mockMvc.perform(get("/ydyo/inbox")
                        .with(user("ydyo1").roles("YDYO")))
                .andExpect(status().isOk());


    }

    private void ydyoValidate() throws Exception {

        mockMvc.perform(post("/ydyo/validate")
                        .with(user("ydyo1").roles("YDYO"))
                        .param("appId", appId.toString()))
                .andExpect(status().isOk());


    }

    private void ydyoPlacementExam() throws Exception {

        mockMvc.perform(post("/ydyo/placement-exam")
                        .with(user("ydyo1").roles("YDYO"))
                        .param("appId", appId.toString())
                        .param("passed", "true"))
                .andExpect(status().isOk());


    }

    private void validateAndSendToFaculty() throws Exception {

        mockMvc.perform(post("/oidb/send-to-faculty")
                        .with(user("oidb1").roles("OIDB"))
                        .param("appId", appId.toString())
                        .param("valid", "true"))
                .andExpect(status().isOk());

    }

    private void sendToYgk() throws Exception {

        mockMvc.perform(post("/oidb/send-to-ygk")
                        .with(user("oidb1").roles("OIDB"))
                        .param("appId", appId.toString()))
                .andExpect(status().isOk());


    }

    private void ygkInbox() throws Exception {

        mockMvc.perform(get("/ygk/inbox")
                        .with(user("ygk1").roles("YGK")))
                .andExpect(status().isOk());


    }

    private void ygkFinalize() throws Exception {

        mockMvc.perform(post("/ygk/finalize")
                        .with(user("ygk1").roles("YGK"))
                        .param("appId", appId.toString())
                        .param("decision", "PRIMARY"))
                .andExpect(status().isOk());


    }

    private void assertStatus() {
        ApplicationStatus current =
                applicationRepository.findById(appId).get().getStatus();

    }
}
