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

}
