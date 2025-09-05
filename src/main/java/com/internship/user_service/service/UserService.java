package com.internship.user_service.service;

import com.internship.user_service.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO createUser(UserDTO user);
    UserDTO getUser(UUID id);
    List<UserDTO> getUsers(List<UUID> ids);
    UserDTO getUserByEmail(String email);
    UserDTO updateUser(UUID id, UserDTO user);
    void deleteUser(UUID id);
}