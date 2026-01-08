package com.utms.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utms.backend.model.record.ApplicationSubmitRequest;
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
public class StudentFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void studentFlow() throws Exception {

        submitApplication();
        getStudentApplications();
        getMyResult();


    }

    private void submitApplication() throws Exception {

        ApplicationSubmitRequest request =
                new ApplicationSubmitRequest( 1L);

        mockMvc.perform(post("/student/submit")
                        .with(user("std1").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }


    private void getStudentApplications() throws Exception {

        mockMvc.perform(get("/student/student/{studentId}", 1L)
                        .with(user("std1").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    private void getMyResult() throws Exception {

        mockMvc.perform(get("/student/student/results")
                        .with(user("std1").roles("STUDENT")))
                .andExpect(status().isOk());
    }
}
