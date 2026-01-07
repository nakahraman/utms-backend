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
public class FacultyFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    void facultyFlow() throws Exception {

        facultyInbox();
        facultyEvaluate();
        facultyReturnToOidb();
    }

    private void facultyInbox() throws Exception {

        mockMvc.perform(get("/faculty/inbox")
                        .with(user("faculty1").roles("FACULTY")))
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
}
