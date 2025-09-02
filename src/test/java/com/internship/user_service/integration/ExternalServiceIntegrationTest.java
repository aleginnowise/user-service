package com.internship.user_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.internship.user_service.dto.UserDTO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
@ActiveProfiles("test")
class ExternalServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static WireMockServer wireMockServer;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    static {
        wireMockServer = new WireMockServer(options().port(0));
        wireMockServer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("external.service.url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeEach
    void setUp() {
        WireMock.configureFor("localhost", wireMockServer.port());
        WireMock.reset();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void createUser_ShouldReturnCreatedUser_WhenExternalServiceValidatesEmail() throws Exception {
        stubFor(get(urlEqualTo("/validation/email/slavakpss@mail.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valid\": true, \"message\": \"Email is valid\"}")));

        UserDTO userDTO = new UserDTO();
        userDTO.setName("Slava");
        userDTO.setSurname("Kpss");
        userDTO.setBirthDate(LocalDate.of(1990, 5, 9));
        userDTO.setEmail("slavakpss@mail.com");

        mockMvc.perform(post("/101FM/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Slava"))
                .andExpect(jsonPath("$.email").value("slavakpss@mail.com"));
    }

    @Test
    void createUser_ShouldReturnCreatedUser_WhenExternalServiceReturnsError() throws Exception {
        stubFor(get(urlEqualTo("/validation/email/invalid@example.com"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valid\": false, \"message\": \"Email domain is blocked\"}")));

        UserDTO userDTO = new UserDTO();
        userDTO.setName("Slava");
        userDTO.setSurname("Kpss");
        userDTO.setBirthDate(LocalDate.of(1990, 5, 9));
        userDTO.setEmail("invalid@example.com");

        mockMvc.perform(post("/101FM/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("invalid@example.com"));
    }

    @Test
    void createUser_ShouldReturnCreatedUser_WhenExternalServiceTimesOut() throws Exception {
        stubFor(get(urlEqualTo("/validation/email/timeout@example.com"))
                .willReturn(aResponse()
                        .withFixedDelay(5000)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valid\": true}")));

        UserDTO userDTO = new UserDTO();
        userDTO.setName("Slava");
        userDTO.setSurname("Kpss");
        userDTO.setBirthDate(LocalDate.of(1990, 5, 9));
        userDTO.setEmail("timeout@example.com");

        mockMvc.perform(post("/101FM/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("timeout@example.com"));
    }

    @Test
    void createUser_ShouldReturnCreatedUser_WhenMultipleExternalServicesCalled() throws Exception {
        stubFor(get(urlEqualTo("/validation/email/multi@example.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valid\": true}")));

        stubFor(get(urlEqualTo("/fraud-check/user/multi@example.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"riskScore\": 0.1, \"approved\": true}")));

        UserDTO userDTO = new UserDTO();
        userDTO.setName("Slava");
        userDTO.setSurname("Kpss");
        userDTO.setBirthDate(LocalDate.of(1990, 5, 9));
        userDTO.setEmail("multi@example.com");

        mockMvc.perform(post("/101FM/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("multi@example.com"));
    }
}