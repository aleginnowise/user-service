package com.internship.user_service.service;

import com.internship.user_service.dto.CardDTO;
import com.internship.user_service.mapper.CardMapper;
import com.internship.user_service.model.Card;
import com.internship.user_service.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public CardServiceImpl(CardRepository cardRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }

    @Override
    @Transactional
    public CardDTO createCard(CardDTO cardDTO) {
        Card card = cardRepository.save(cardMapper.cardDTOToCard(cardDTO));

        return cardMapper.cardToCardDTO(card);
    }

    @Override
    public CardDTO getCard(UUID id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card with id " + id + " does not exist"));

        return cardMapper.cardToCardDTO(card);
    }

    @Override
    public List<CardDTO> getCards(List<UUID> ids) {
        List<Card> cards = cardRepository.findAllById(ids);

        return cards.stream()
                .map(cardMapper::cardToCardDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CardDTO updateCard(UUID id, CardDTO cardDTO) {
        Card card = cardMapper.cardDTOToCard(cardDTO);
        Card updatedCard = cardRepository.findById(id)
                .map(repoCard -> {
                    repoCard.setUser(card.getUser());
                    repoCard.setNumber(card.getNumber());
                    repoCard.setHolder(card.getHolder());
                    repoCard.setExpirationDate(card.getExpirationDate());
                    return cardRepository.save(repoCard);
                })
                .orElseThrow(() -> new RuntimeException("Card with id " + id + " does not exist"));

        return cardMapper.cardToCardDTO(updatedCard);
    }

    @Override
    @Transactional
    public void deleteCard(UUID id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card with id " + id + " does not exist"));
        cardRepository.delete(card);
    }
}