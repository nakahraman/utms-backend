package com.utms.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utms.backend.externalIntegration.DocumentVerificationService;
import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import com.utms.backend.model.record.LoginRequest;
import com.utms.backend.model.record.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExternalDraftFlowIT {

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

        mockDocumentVerification();   // ❗ BURASI

        registerExternal();
        login();
        createDraft();
        uploadTranscript();
        uploadYksResult();
        uploadEngCert();
        submitExternalApplication();

    }

    private void mockDocumentVerification() {
        when(documentVerificationService.verify(
                any(DocumentType.class),
                any(TransferDocument.class)
        )).thenReturn(true);
    }


    private void registerExternal() throws Exception {

        RegisterRequest req = new RegisterRequest(
                "std101",
                "1",
                "ali@test.com",
                "Veli"
        );

        mockMvc.perform(post("/auth/register-external")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    private void login() throws Exception {

        LoginRequest req = new LoginRequest("std101", "1");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        this.jwt = result.getResponse().getContentAsString();
        assertNotNull(jwt);
    }

    private void createDraft() throws Exception {

        MvcResult result = mockMvc.perform(post("/student/ext-draft")
                        .header("Authorization", "Bearer " + jwt)
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andReturn();

        this.appId = Long.valueOf(result.getResponse().getContentAsString());
        assertNotNull(appId);
        System.out.println(result.getResponse().getContentAsString());
    }

    private void uploadTranscript() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transcript.pdf",
                "application/pdf",
                "FAKE PDF CONTENT".getBytes()
        );

        mockMvc.perform(multipart("/documents/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt)
                        .param("appId", appId.toString())
                        .param("documentType", "TRANSCRIPT")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    private void uploadYksResult() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transcript.pdf",
                "application/pdf",
                "FAKE PDF CONTENT".getBytes()
        );

        mockMvc.perform(multipart("/documents/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt)
                        .param("appId", appId.toString())
                        .param("documentType", "YKS_RESULT")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    private void uploadEngCert() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transcript.pdf",
                "application/pdf",
                "FAKE PDF CONTENT".getBytes()
        );

        mockMvc.perform(multipart("/documents/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt)
                        .param("appId", appId.toString())
                        .param("documentType", "ENGLISH_CERTIFICATE")
                        .with(csrf()))
                .andExpect(status().isOk());
    }


    private void submitExternalApplication() throws Exception {

        mockMvc.perform(post("/student/ext-submit/{appId}", 1L)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());
    }

}

