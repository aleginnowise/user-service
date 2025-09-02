package com.internship.user_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.user_service.dto.CardDTO;
import com.internship.user_service.dto.UserDTO;
import com.internship.user_service.repository.CardRepository;
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
class CardControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private UUID testUserId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        cardRepository.deleteAll();
        userRepository.deleteAll();
        
        testUserId = createTestUser();
    }

    @Test
    void createCard_ShouldReturnCreatedCard_WhenValidInput() throws Exception {
        CardDTO cardDTO = createTestCardDTO(testUserId);

        MvcResult result = performCreateCardRequest(cardDTO);

        String responseContent = result.getResponse().getContentAsString();
        CardDTO createdCard = objectMapper.readValue(responseContent, CardDTO.class);
        assertThat(createdCard.getId()).isNotNull();
        assertThat(cardRepository.count()).isEqualTo(1);
    }

    @Test
    void createCard_ShouldReturnValidationErrors_WhenInvalidInput() throws Exception {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setUserId(testUserId);
        cardDTO.setNumber("123");
        cardDTO.setHolder("");
        cardDTO.setExpirationDate(LocalDate.of(2020, 1, 1));

        mockMvc.perform(post("/101FM/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDTO)))
                .andExpect(status().isBadRequest());

        assertThat(cardRepository.count()).isZero();
    }

    @Test
    void getCard_ShouldReturnCard_WhenCardExists() throws Exception {
        CardDTO cardDTO = createTestCardDTO(testUserId);
        CardDTO createdCard = createCardViaApi(cardDTO);

        mockMvc.perform(get("/101FM/cards/{id}", createdCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdCard.getId().toString()))
                .andExpect(jsonPath("$.number").value("1111111111111111"))
                .andExpect(jsonPath("$.holder").value("SLAVA KPSS"))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));
    }

    @Test
    void getCard_ShouldReturnNotFound_WhenCardDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/101FM/cards/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCards_ShouldReturnListOfCards_WhenCardsExist() throws Exception {
        CardDTO firstCard = createTestCardDTO(testUserId);
        firstCard.setNumber("1111222233334444");
        CardDTO createdFirstCard = createCardViaApi(firstCard);

        CardDTO secondCard = createTestCardDTO(testUserId);
        secondCard.setNumber("5555666677778888");
        secondCard.setHolder("LISA OOES");
        CardDTO createdSecondCard = createCardViaApi(secondCard);

        List<UUID> cardIds = List.of(createdFirstCard.getId(), createdSecondCard.getId());

        mockMvc.perform(get("/101FM/cards")
                        .param("ids", cardIds.get(0).toString(), cardIds.get(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(createdFirstCard.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(createdSecondCard.getId().toString()));
    }

    @Test
    void updateCard_ShouldReturnUpdatedCard_WhenCardExists() throws Exception {
        CardDTO cardDTO = createTestCardDTO(testUserId);
        CardDTO createdCard = createCardViaApi(cardDTO);

        CardDTO updateDTO = new CardDTO();
        updateDTO.setUserId(testUserId);
        updateDTO.setNumber("9999888877776666");
        updateDTO.setHolder("UPDATED SLAVA");
        updateDTO.setExpirationDate(LocalDate.of(2027, 8, 20));

        mockMvc.perform(put("/101FM/cards/{id}", createdCard.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdCard.getId().toString()))
                .andExpect(jsonPath("$.number").value("9999888877776666"))
                .andExpect(jsonPath("$.holder").value("UPDATED SLAVA"));
    }

    @Test
    void updateCard_ShouldReturnNotFound_WhenCardDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        CardDTO updateDTO = createTestCardDTO(testUserId);

        mockMvc.perform(put("/101FM/cards/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCard_ShouldDeleteCard_WhenCardExists() throws Exception {
        CardDTO cardDTO = createTestCardDTO(testUserId);
        CardDTO createdCard = createCardViaApi(cardDTO);

        mockMvc.perform(delete("/101FM/cards/{id}", createdCard.getId()))
                .andExpect(status().isNoContent());

        assertThat(cardRepository.count()).isZero();
    }

    @Test
    void deleteCard_ShouldReturnNotFound_WhenCardDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/101FM/cards/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void crudOperations_ShouldWorkEndToEnd() throws Exception {
        CardDTO cardDTO = createTestCardDTO(testUserId);
        CardDTO createdCard = createCardViaApi(cardDTO);
        assertThat(createdCard.getId()).isNotNull();

        mockMvc.perform(get("/101FM/cards/{id}", createdCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("1111111111111111"));

        CardDTO updateDTO = new CardDTO();
        updateDTO.setUserId(testUserId);
        updateDTO.setNumber("9999888877776666");
        updateDTO.setHolder("UPDATED SLAVA");
        updateDTO.setExpirationDate(LocalDate.of(2027, 8, 20));

        mockMvc.perform(put("/101FM/cards/{id}", createdCard.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("9999888877776666"));

        mockMvc.perform(delete("/101FM/cards/{id}", createdCard.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/101FM/cards/{id}", createdCard.getId()))
                .andExpect(status().isNotFound());
    }

    private UUID createTestUser() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Slava");
        userDTO.setSurname("Kpss");
        userDTO.setBirthDate(LocalDate.of(1990, 5, 9));
        userDTO.setEmail("slavakpss@mail.com");

        String createdUserJson = mockMvc.perform(post("/101FM/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserDTO createdUser = objectMapper.readValue(createdUserJson, UserDTO.class);
        return createdUser.getId();
    }

    private CardDTO createCardViaApi(CardDTO cardDTO) throws Exception {
        String createdCardJson = mockMvc.perform(post("/101FM/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(createdCardJson, CardDTO.class);
    }

    private MvcResult performCreateCardRequest(CardDTO cardDTO) throws Exception {
        return mockMvc.perform(post("/101FM/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("1111111111111111"))
                .andExpect(jsonPath("$.holder").value("SLAVA KPSS"))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
    }

    private CardDTO createTestCardDTO(UUID userId) {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setUserId(userId);
        cardDTO.setNumber("1111111111111111");
        cardDTO.setHolder("SLAVA KPSS");
        cardDTO.setExpirationDate(LocalDate.of(2035, 12, 31));
        return cardDTO;
    }
}