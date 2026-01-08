package com.utms.backend;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.utms.backend.externalIntegration.DocumentVerificationService;
import com.utms.backend.model.record.ApplicationSubmitRequest;
import com.utms.backend.model.record.LoginRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional

public class InterbalDraftFlowIT {

    @Autowired
    private MockMvc mockMvc;

    private Long appId;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwt;

    @MockBean
    private DocumentVerificationService documentVerificationService;

    @Test
    void externalDraftFlow() throws Exception {

        mockDocumentVerification();
        login();
        createDraft();
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
        facultyEvaluate();

        //oidb
        getOidbInbox();
        sendToYgk();
        //oidb
        getOidbInbox();
        publishResults();

        //student
        getMyResult();

    }

    private void mockDocumentVerification() {
        Mockito.when(documentVerificationService.verify(Mockito.any(), Mockito.any()))
                .thenReturn(true);
    }
    private void login() throws Exception {

        LoginRequest req = new LoginRequest("std1", "1");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        this.jwt = result.getResponse().getContentAsString();
        assertNotNull(jwt);
    }

    private void createDraft() throws Exception {

        MvcResult result = mockMvc.perform(post("/student/draft")
                        .header("Authorization", "Bearer " + jwt)
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        this.appId = Long.valueOf(body);

        assertNotNull(appId);
        System.out.println("app id " + appId);
    }

    private void submitApplication() throws Exception {

        mockMvc.perform(post("/student/submit/{appId}", appId)
                        .header("Authorization", "Bearer " + jwt))
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

    private void facultyInbox() throws Exception {

        mockMvc.perform(get("/faculty/inbox")
                        .with(user("fac1").roles("FACULTY")))
                .andExpect(status().isOk());
    }


    private void facultyReturnToOidb() throws Exception {

        mockMvc.perform(post("/faculty/return")
                        .with(user("fac1").roles("FACULTY"))
                        .param("appId", "1")
                        .param("reason", "Eksik belge"))
                .andExpect(status().isOk());
    }

    private void resendToFaculty() throws Exception {

        mockMvc.perform(post("/oidb/resend-to-faculty")
                        .with(user("oidb1").roles("OIDB"))
                        .param("appId", "1"))
                .andExpect(status().isOk());
    }
    private void facultyEvaluate() throws Exception {

        mockMvc.perform(post("/faculty/evaluate")
                        .with(user("fac1").roles("FACULTY"))
                        .param("quota", "5"))
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
}
