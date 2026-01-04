package com.utms.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UTMSFlowIT {
/*
    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullTransferFlow() throws Exception {

        mockMvc.perform(post("/applications/submit")
                        .with(httpBasic("student1", "1234"))
                        .param("studentId", "1")
                        .param("departmentId", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/applications/oidb/validate")
                        .with(httpBasic("oidb1", "1234"))
                        .param("appId", "1")
                        .param("valid", "true"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/faculty/send-to-department")
                        .with(httpBasic("faculty1", "1234"))
                        .param("appId", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ygk/evaluate")
                        .with(httpBasic("ygk1", "1234"))
                        .param("quota", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/faculty/approve")
                        .with(httpBasic("faculty1", "1234"))
                        .param("appId", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/registrar/receive")
                        .with(httpBasic("oidb1", "1234"))
                        .param("appId", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/registrar/publish-results")
                        .with(httpBasic("oidb1", "1234")))
                .andExpect(status().isOk());
    }

    @Test
    void fullFlow() throws Exception {

        mockMvc.perform(post("/applications/submit")
                        .with(httpBasic("student1","1234"))
                        .param("studentId","1")
                        .param("departmentId","1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/applications/oidb/validate")
                        .with(httpBasic("oidb1","1234"))
                        .param("appId","1")
                        .param("valid","true"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/faculty/send-to-department")
                        .with(httpBasic("faculty1","1234"))
                        .param("appId","1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ygk/evaluate")
                        .with(httpBasic("ygk1","1234"))
                        .param("quota","1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/faculty/approve")
                        .with(httpBasic("faculty1","1234"))
                        .param("appId","1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/registrar/receive")
                        .with(httpBasic("oidb1","1234"))
                        .param("appId","1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/registrar/publish-results")
                        .with(httpBasic("oidb1","1234")))
                .andExpect(status().isOk());
    }

 */
}
