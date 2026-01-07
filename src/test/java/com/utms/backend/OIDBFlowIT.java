package com.utms.backend.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utms.backend.model.record.ApplicationSubmitRequest;
import com.utms.backend.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class OIDBFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    void fullTransferFlow() throws Exception {

        getOidbInbox();
        sendToYdyo();
        validateAndSendToFaculty();
        resendToFaculty();
        sendToYgk();
        publishResults();
        getFinalResults();
        getPublishedResults();
    }


    private void getOidbInbox() throws Exception {

        mockMvc.perform(get("/oidb/inbox")
                        .with(user("oidb1").roles("OIDB")))
                .andExpect(status().isOk());
    }

    private void sendToYdyo() throws Exception {

        mockMvc.perform(post("/oidb/send-to-ydyo")
                        .with(user("oidb1").roles("OIDB"))
                        .param("appId", "1"))
                .andExpect(status().isOk());
    }

    private void validateAndSendToFaculty() throws Exception {

        mockMvc.perform(post("/oidb/send-to-faculty")
                        .with(user("oidb1").roles("OIDB"))
                        .param("appId", "1")
                        .param("valid", "true"))
                .andExpect(status().isOk());
    }

    private void sendToYgk() throws Exception {

        mockMvc.perform(post("/oidb/send-to-ygk")
                        .with(user("oidb1").roles("OIDB"))
                        .param("appId", "1"))
                .andExpect(status().isOk());
    }

    private void publishResults() throws Exception {

        mockMvc.perform(post("/oidb/publish-results")
                        .with(user("oidb1").roles("OIDB")))
                .andExpect(status().isOk());
    }

    private void resendToFaculty() throws Exception {

        mockMvc.perform(post("/oidb/resend-to-faculty")
                        .with(user("oidb1").roles("OIDB"))
                        .param("appId", "1"))
                .andExpect(status().isOk());
    }

    private void getFinalResults() throws Exception {

        mockMvc.perform(get("/oidb/results")
                        .with(user("oidb1").roles("OIDB")))
                .andExpect(status().isOk());
    }

    private void getPublishedResults() throws Exception {

        mockMvc.perform(get("/oidb/results")
                        .with(user("oidb1").roles("OIDB"))
                        .param("published", "true"))
                .andExpect(status().isOk());
    }
}
