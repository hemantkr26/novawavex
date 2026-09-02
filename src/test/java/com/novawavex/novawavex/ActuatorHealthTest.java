package com.novawavex.novawavex;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ActuatorHealthTest {

    @Autowired
    private MockMvc mockMvc;


    // =========================================================
    // TEST 1
    // ACTUATOR HEALTH SHOULD BE UP
    // =========================================================

    @Test
    void health_shouldReturnUp() throws Exception {

        mockMvc.perform(
                get("/actuator/health")
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$.status")
                        .value("UP")
        );
    }
}