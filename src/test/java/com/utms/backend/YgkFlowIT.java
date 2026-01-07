package com.utms.backend;

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
public class YgkFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ygkFlow() throws Exception {

        ygkInbox();
        ygkFinalize();
    }

    private void ygkInbox() throws Exception {

        mockMvc.perform(get("/ygk/inbox")
                        .with(user("ygk1").roles("YGK")))
                .andExpect(status().isOk());
    }

    private void ygkFinalize() throws Exception {

        mockMvc.perform(post("/ygk/finalize")
                        .with(user("ygk1").roles("YGK"))
                        .param("appId", "1")
                        .param("decision", "PRIMARY"))
                .andExpect(status().isOk());
    }
}
