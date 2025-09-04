package com.internship.user_service.service;

import com.internship.user_service.dto.CardDTO;
import com.internship.user_service.exception.custom_exceptions.CardNotFoundException;
import com.internship.user_service.mapper.CardMapper;
import com.internship.user_service.model.Card;
import com.internship.user_service.model.User;
import com.internship.user_service.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card testCard;
    private CardDTO testCardDTO;
    private UUID testCardId;
    private String testNumber;
    private UUID testUserId;
    private User testUser;
    private String testName;
    private String testSurname;
    LocalDate testBirthDate;
    private String testEmail;
    private String testHolder;
    private LocalDate testExpirationDate;

    @BeforeEach
    void setUp() {
        testCardId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        testNumber = "1111111111111111";

        testUserId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        testName = "Slava";
        testSurname = "Kpss";
        testBirthDate = LocalDate.of(1990, 5, 9);
        testEmail = "slavakpss@mail.com";
        testHolder = "SLAVA KPSS";
        testExpirationDate = LocalDate.of(2035, 12, 31);

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setName(testName);
        testUser.setSurname(testSurname);
        testUser.setBirthDate(testBirthDate);
        testUser.setEmail(testEmail);

        testCard = new Card();
        testCard.setId(testCardId);
        testCard.setUser(testUser);
        testCard.setNumber(testNumber);
        testCard.setHolder(testHolder);
        testCard.setExpirationDate(testExpirationDate);

        testCardDTO = new CardDTO();
        testCardDTO.setId(testCardId);
        testCardDTO.setUserId(testUserId);
        testCardDTO.setNumber(testNumber);
        testCardDTO.setHolder(testHolder);
        testCardDTO.setExpirationDate(testExpirationDate);
    }

    @Test
    void createCard_ShouldReturnCardDTO_WhenValidInput() {
        CardDTO inputDTO = new CardDTO();
        inputDTO.setUserId(testUserId);
        inputDTO.setNumber(testNumber);
        inputDTO.setHolder(testHolder);
        inputDTO.setExpirationDate(testExpirationDate);

        when(cardMapper.cardDTOToCard(inputDTO)).thenReturn(testCard);
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardMapper.cardToCardDTO(testCard)).thenReturn(testCardDTO);

        CardDTO result = cardService.createCard(inputDTO);

        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(testNumber);
        assertThat(result.getHolder()).isEqualTo(testHolder);
        assertThat(result.getUserId()).isEqualTo(testUserId);

        verify(cardRepository).save(testCard);
        verify(cardMapper).cardDTOToCard(inputDTO);
        verify(cardMapper).cardToCardDTO(testCard);
    }

    @Test
    void getCard_ShouldReturnCardDTO_WhenCardExists() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
        when(cardMapper.cardToCardDTO(testCard)).thenReturn(testCardDTO);

        CardDTO result = cardService.getCard(testCardId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testCardId);
        assertThat(result.getNumber()).isEqualTo(testNumber);
        assertThat(result.getUserId()).isEqualTo(testUserId);

        verify(cardRepository).findById(testCardId);
        verify(cardMapper).cardToCardDTO(testCard);
    }

    @Test
    void getCards_ShouldReturnListOfCardDTOs_WhenCardsExist() {
        UUID secondCardId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        String secondNumber = "2222222222222222";
        String secondHolder = "LISA OOES";
        LocalDate secondExpirationDate = LocalDate.of(2040, 8, 15);
        
        List<UUID> cardIds = List.of(testCardId, secondCardId);

        Card secondCard = new Card();
        secondCard.setId(secondCardId);
        secondCard.setUser(testUser);
        secondCard.setNumber(secondNumber);
        secondCard.setHolder(secondHolder);
        secondCard.setExpirationDate(secondExpirationDate);

        CardDTO secondCardDTO = new CardDTO();
        secondCardDTO.setId(secondCardId);
        secondCardDTO.setUserId(testUserId);
        secondCardDTO.setNumber(secondNumber);
        secondCardDTO.setHolder(secondHolder);
        secondCardDTO.setExpirationDate(secondExpirationDate);

        List<Card> cards = List.of(testCard, secondCard);

        when(cardRepository.findAllById(cardIds)).thenReturn(cards);
        when(cardMapper.cardToCardDTO(testCard)).thenReturn(testCardDTO);
        when(cardMapper.cardToCardDTO(secondCard)).thenReturn(secondCardDTO);

        List<CardDTO> result = cardService.getCards(cardIds);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(testCardId);
        assertThat(result.get(1).getId()).isEqualTo(secondCardId);

        verify(cardRepository).findAllById(cardIds);
    }

    @Test
    void getCards_ShouldReturnEmptyList_WhenNoCardsExist() {
        List<UUID> cardIds = List.of(testCardId);

        when(cardRepository.findAllById(cardIds)).thenReturn(List.of());

        List<CardDTO> result = cardService.getCards(cardIds);

        assertThat(result).isEmpty();

        verify(cardRepository).findAllById(cardIds);
    }

    @Test
    void updateCard_ShouldReturnUpdatedCardDTO_WhenCardExists() {
        String updatedNumber = "3333333333333333";
        String updatedHolder = "UPDATED SLAVA";
        LocalDate updatedExpirationDate = LocalDate.of(2040, 1, 1);
        
        CardDTO updateDTO = new CardDTO();
        updateDTO.setUserId(testUserId);
        updateDTO.setNumber(updatedNumber);
        updateDTO.setHolder(updatedHolder);
        updateDTO.setExpirationDate(updatedExpirationDate);

        Card mappedCard = new Card();
        mappedCard.setUser(testUser);
        mappedCard.setNumber(updatedNumber);
        mappedCard.setHolder(updatedHolder);
        mappedCard.setExpirationDate(updatedExpirationDate);

        Card updatedCard = new Card();
        updatedCard.setId(testCardId);
        updatedCard.setUser(testUser);
        updatedCard.setNumber(updatedNumber);
        updatedCard.setHolder(updatedHolder);
        updatedCard.setExpirationDate(updatedExpirationDate);

        CardDTO updatedDTO = new CardDTO();
        updatedDTO.setId(testCardId);
        updatedDTO.setUserId(testUserId);
        updatedDTO.setNumber(updatedNumber);
        updatedDTO.setHolder(updatedHolder);
        updatedDTO.setExpirationDate(updatedExpirationDate);

        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard), Optional.of(testCard));
        when(cardMapper.cardDTOToCard(updateDTO)).thenReturn(mappedCard);
        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);
        when(cardMapper.cardToCardDTO(updatedCard)).thenReturn(updatedDTO);

        CardDTO result = cardService.updateCard(testCardId, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(updatedNumber);
        assertThat(result.getHolder()).isEqualTo(updatedHolder);
        assertThat(result.getId()).isEqualTo(testCardId);

        verify(cardRepository).findById(testCardId);
        verify(cardRepository).save(any(Card.class));
        verify(cardMapper).cardDTOToCard(updateDTO);
        verify(cardMapper).cardToCardDTO(updatedCard);
    }

    @Test
    void deleteCard_ShouldDeleteCard_WhenCardExists() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        cardService.deleteCard(testCardId);

        verify(cardRepository).findById(testCardId);
        verify(cardRepository).delete(testCard);
    }

    @Test
    void deleteCard_ShouldThrowCardNotFoundException_WhenCardDoesNotExist() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deleteCard(testCardId))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Card with id " + testCardId + " does not exist");

        verify(cardRepository).findById(testCardId);
        verify(cardRepository, never()).delete(any());
    }
}