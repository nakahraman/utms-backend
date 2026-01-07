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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UTMSStateFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Long appId = 1L;

    @Test
    void utmsStateFlow() throws Exception {

        submitApplication(ApplicationStatus.DRAFT);
        getOidbInbox(ApplicationStatus.SUBMITTED);
        sendToYdyo(ApplicationStatus.SENT_TO_YDYO);

        ydyoInbox(ApplicationStatus.SENT_TO_YDYO);
        ydyoValidate(ApplicationStatus.YDYO_EXAM_REQUIRED);
        ydyoPlacementExam(ApplicationStatus.YDYO_APPROVED);

        getOidbInbox(ApplicationStatus.YDYO_APPROVED);
        validateAndSendToFaculty(ApplicationStatus.OIDB_VALIDATED);

        getOidbInbox(ApplicationStatus.OIDB_VALIDATED);
        sendToYgk(ApplicationStatus.FACULTY_EVALUATED);

        ygkInbox(ApplicationStatus.FACULTY_EVALUATED);
        ygkFinalize(ApplicationStatus.SENT_TO_YGK);
    }

    private void submitApplication(ApplicationStatus expected) throws Exception {

        ApplicationSubmitRequest req = new ApplicationSubmitRequest(1L,1L);

        mockMvc.perform(post("/student/submit")
                        .with(user("std1").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        assertStatus(expected);
    }

    private void getOidbInbox(ApplicationStatus expected) throws Exception {

        mockMvc.perform(get("/oidb/inbox")
                        .with(user("oidb1").roles("OIDB")))
                .andExpect(status().isOk());

        assertStatus(expected);
    }

    private void sendToYdyo(ApplicationStatus expected) throws Exception {

        mockMvc.perform(post("/oidb/send-to-ydyo")
                        .with(user("oidb1").roles("OIDB"))
                        .param("appId", appId.toString()))
                .andExpect(status().isOk());

        assertStatus(expected);
    }

    private void ydyoInbox(ApplicationStatus expected) throws Exception {

        mockMvc.perform(get("/ydyo/inbox")
                        .with(user("ydyo1").roles("YDYO")))
                .andExpect(status().isOk());

        assertStatus(expected);
    }

    private void ydyoValidate(ApplicationStatus expected) throws Exception {

        mockMvc.perform(post("/ydyo/validate")
                        .with(user("ydyo1").roles("YDYO"))
                        .param("appId", appId.toString()))
                .andExpect(status().isOk());

        assertStatus(expected);
    }

    private void ydyoPlacementExam(ApplicationStatus expected) throws Exception {

        mockMvc.perform(post("/ydyo/placement-exam")
                        .with(user("ydyo1").roles("YDYO"))
                        .param("appId", appId.toString())
                        .param("passed", "true"))
                .andExpect(status().isOk());

        assertStatus(expected);
    }

    private void validateAndSendToFaculty(ApplicationStatus expected) throws Exception {

        mockMvc.perform(post("/oidb/send-to-faculty")
                        .with(user("oidb1").roles("OIDB"))
                        .param("appId", appId.toString())
                        .param("valid", "true"))
                .andExpect(status().isOk());

        assertStatus(expected);
    }

    private void sendToYgk(ApplicationStatus expected) throws Exception {

        mockMvc.perform(post("/oidb/send-to-ygk")
                        .with(user("oidb1").roles("OIDB"))
                        .param("appId", appId.toString()))
                .andExpect(status().isOk());

        assertStatus(expected);
    }

    private void ygkInbox(ApplicationStatus expected) throws Exception {

        mockMvc.perform(get("/ygk/inbox")
                        .with(user("ygk1").roles("YGK")))
                .andExpect(status().isOk());

        assertStatus(expected);
    }

    private void ygkFinalize(ApplicationStatus expected) throws Exception {

        mockMvc.perform(post("/ygk/finalize")
                        .with(user("ygk1").roles("YGK"))
                        .param("appId", appId.toString())
                        .param("decision", "PRIMARY"))
                .andExpect(status().isOk());

        assertStatus(expected);
    }

    private void assertStatus(ApplicationStatus expected) {
        ApplicationStatus current =
                applicationRepository.findById(appId).get().getStatus();
        assertThat(current).isEqualTo(expected);
    }
}
