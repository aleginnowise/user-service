package com.internship.user_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.user_service.dto.UserDTO;
import com.internship.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();
    }

    @Test
    void createUser_ShouldReturnCreatedUser_WhenValidInput() throws Exception {
        UserDTO userDTO = createTestUser();

        MvcResult result = performCreateUserRequest(userDTO);

        String responseContent = result.getResponse().getContentAsString();
        UserDTO createdUser = objectMapper.readValue(responseContent, UserDTO.class);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void createUser_ShouldReturnValidationErrors_WhenInvalidInput() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("");
        userDTO.setSurname("");
        userDTO.setBirthDate(LocalDate.of(2030, 1, 1));
        userDTO.setEmail("invalid-email");

        mockMvc.perform(post("/101FM/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void getUser_ShouldReturnUser_WhenUserExists() throws Exception {
        UserDTO userDTO = createTestUser();
        UserDTO createdUser = createUserViaApi(userDTO);

        mockMvc.perform(get("/101FM/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId().toString()))
                .andExpect(jsonPath("$.name").value("Slava"))
                .andExpect(jsonPath("$.surname").value("Kpss"))
                .andExpect(jsonPath("$.email").value("slavakpss@mail.com"));
    }

    @Test
    void getUser_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/101FM/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUsers_ShouldReturnListOfUsers_WhenUsersExist() throws Exception {
        UserDTO firstUser = createTestUser();
        firstUser.setEmail("slavakpss@mail.com");
        UserDTO createdFirstUser = createUserViaApi(firstUser);

        UserDTO secondUser = createTestUser();
        secondUser.setEmail("ooes@mail.com");
        secondUser.setName("Lisa");
        UserDTO createdSecondUser = createUserViaApi(secondUser);

        List<UUID> userIds = List.of(createdFirstUser.getId(), createdSecondUser.getId());

        mockMvc.perform(get("/101FM/users")
                        .param("ids", userIds.get(0).toString(), userIds.get(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(createdFirstUser.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(createdSecondUser.getId().toString()));
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenUserExists() throws Exception {
        UserDTO userDTO = createTestUser();
        createUserViaApi(userDTO);

        mockMvc.perform(get("/101FM/users/email/{email}", "slavakpss@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("slavakpss@mail.com"))
                .andExpect(jsonPath("$.name").value("Slava"));
    }

    @Test
    void getUserByEmail_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/101FM/users/email/{email}", "nonexistent@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUserExists() throws Exception {
        UserDTO userDTO = createTestUser();
        UserDTO createdUser = createUserViaApi(userDTO);

        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("Updated Slava");
        updateDTO.setSurname("Updated Kpss");
        updateDTO.setBirthDate(LocalDate.of(1985, 5, 15));
        updateDTO.setEmail("updated@mail.com");

        mockMvc.perform(put("/101FM/users/{id}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId().toString()))
                .andExpect(jsonPath("$.name").value("Updated Slava"))
                .andExpect(jsonPath("$.surname").value("Updated Kpss"))
                .andExpect(jsonPath("$.email").value("updated@mail.com"));
    }

    @Test
    void updateUser_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        UserDTO updateDTO = createTestUser();

        mockMvc.perform(put("/101FM/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() throws Exception {
        UserDTO userDTO = createTestUser();
        UserDTO createdUser = createUserViaApi(userDTO);

        mockMvc.perform(delete("/101FM/users/{id}", createdUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void deleteUser_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/101FM/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void crudOperations_ShouldWorkEndToEnd() throws Exception {
        UserDTO userDTO = createTestUser();
        UserDTO createdUser = createUserViaApi(userDTO);
        assertThat(createdUser.getId()).isNotNull();

        mockMvc.perform(get("/101FM/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Slava"));

        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("Updated Slava");
        updateDTO.setSurname("Updated Kpss");
        updateDTO.setBirthDate(LocalDate.of(1985, 5, 15));
        updateDTO.setEmail("updated@mail.com");

        mockMvc.perform(put("/101FM/users/{id}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Slava"));

        mockMvc.perform(delete("/101FM/users/{id}", createdUser.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/101FM/users/{id}", createdUser.getId()))
                .andExpect(status().isNotFound());
    }

    private UserDTO createTestUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Slava");
        userDTO.setSurname("Kpss");
        userDTO.setBirthDate(LocalDate.of(1990, 5, 9));
        userDTO.setEmail("slavakpss@mail.com");
        return userDTO;
    }

    private UserDTO createUserViaApi(UserDTO userDTO) throws Exception {
        String createdUserJson = mockMvc.perform(post("/101FM/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(createdUserJson, UserDTO.class);
    }

    private MvcResult performCreateUserRequest(UserDTO userDTO) throws Exception {
        return mockMvc.perform(post("/101FM/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Slava"))
                .andExpect(jsonPath("$.surname").value("Kpss"))
                .andExpect(jsonPath("$.email").value("slavakpss@mail.com"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
    }
}