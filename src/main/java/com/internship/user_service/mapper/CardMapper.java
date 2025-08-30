package com.internship.user_service.mapper;

import com.internship.user_service.dto.CardDTO;
import com.internship.user_service.model.Card;
import com.internship.user_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(source = "userId", target = "user")
    Card cardDTOToCard(CardDTO cardDTO);

    @Mapping(source = "user.id", target = "userId")
    CardDTO cardToCardDTO(Card card);

    default User map(UUID userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }

    default UUID map(User user) {
        if (user == null) {
            return null;
        }
        return user.getId();
    }
}