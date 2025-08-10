package com.internship.user_service.mapper;

import com.internship.user_service.dto.UserDTO;
import com.internship.user_service.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User userDTOToUser(UserDTO userDTO);
    UserDTO userToUserDTO(User user);
}