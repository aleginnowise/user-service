package com.internship.user_service.service;

import com.internship.user_service.dto.CardDTO;

import java.util.List;
import java.util.UUID;

public interface CardService {
    CardDTO createCard(CardDTO card);
    CardDTO getCard(UUID id);
    List<CardDTO> getCards(List<UUID> ids);
    CardDTO updateCard(UUID id, CardDTO card);
    void deleteCard(UUID id);
}