package com.utms.backend;

import com.utms.backend.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class YdyoFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    void ydyoFlow() throws Exception {

        ydyoInbox();
        ydyoValidate();
        ydyoPlacementExam();
    }
    @Test
    private void ydyoInbox() throws Exception {

        mockMvc.perform(get("/ydyo/inbox")
                        .with(user("ydyo1").roles("YDYO")))
                .andExpect(status().isOk());
    }

    private void ydyoValidate() throws Exception {

        mockMvc.perform(post("/ydyo/validate")
                        .with(user("ydyo1").roles("YDYO"))
                        .param("appId", "1"))
                .andExpect(status().isOk());
    }

    private void ydyoPlacementExam() throws Exception {

        mockMvc.perform(post("/ydyo/placement-exam")
                        .with(user("ydyo1").roles("YDYO"))
                        .param("appId", "1")
                        .param("passed", "true"))
                .andExpect(status().isOk());
    }
}
