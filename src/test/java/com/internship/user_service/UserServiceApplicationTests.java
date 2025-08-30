package com.internship.user_service;

import com.internship.user_service.dto.CardDTO;
import com.internship.user_service.dto.UserDTO;
import com.internship.user_service.mapper.CardMapper;
import com.internship.user_service.mapper.UserMapper;
import com.internship.user_service.model.Card;
import com.internship.user_service.model.User;
import com.internship.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class UserServiceApplicationTests {

    UserMapper userMapper;
    CardMapper cardMapper;
    @Autowired
    private UserService userService;

    @Autowired
    public UserServiceApplicationTests(UserMapper userMapper, CardMapper cardMapper) {
        this.userMapper = userMapper;
        this.cardMapper = cardMapper;
    }

    @Test
    void contextLoads() {
    }

    @Test
    public void givenUserDTOToUser_whenMaps_thenCorrect(){
        UserDTO userDTO = new UserDTO();
        userDTO.setName("JPEG");
        userDTO.setSurname("101");
        userDTO.setBirthDate(LocalDate.of(2025, Month.JANUARY, 1));
        userDTO.setEmail("101FM@mail.com");

        User user = userMapper.userDTOToUser(userDTO);

        assertEquals(userDTO.getName(), user.getName());
        assertEquals(userDTO.getSurname(), user.getSurname());
        assertEquals(userDTO.getBirthDate(), user.getBirthDate());
        assertEquals(userDTO.getEmail(), user.getEmail());
    }

    @Test
    public void givenCardDTOToCard_whenMaps_thenCorrect(){
        User user = userMapper.userDTOToUser(userService.getUserByEmail("101FM@mail.com"));
        UUID uId = user.getId();

        CardDTO cardDTO = new CardDTO();
        cardDTO.setUserId(uId);
        cardDTO.setNumber("1010_1010_1010_1010");
        cardDTO.setHolder("JPEG");
        cardDTO.setExpirationDate(LocalDate.of(2035, Month.JANUARY, 1));

        Card card = cardMapper.cardDTOToCard(cardDTO);

        assertEquals(cardDTO.getUserId(), card.getUser().getId());
        assertEquals(cardDTO.getNumber(), card.getNumber());
        assertEquals(cardDTO.getHolder(), card.getHolder());
        assertEquals(cardDTO.getExpirationDate(), card.getExpirationDate());
    }
}