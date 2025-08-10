package com.internship.user_service.service;

import com.internship.user_service.dto.UserDTO;
import com.internship.user_service.mapper.UserMapper;
import com.internship.user_service.model.User;
import com.internship.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,  UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO){
        User user = userRepository.save(userMapper.userDTOToUser(userDTO));

        return userMapper.userToUserDTO(user);
    }

    @Override
    public UserDTO getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with id " + id + " does not exist"));

        return userMapper.userToUserDTO(user);
    }

    @Override
    public List<UserDTO> getUsers(List<UUID> ids) {
        List<User> users = userRepository.findAllById(ids);

        return users.stream()
                .map(userMapper::userToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email " + email + " does not exist"));

        return userMapper.userToUserDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(UUID id, UserDTO userDTO) {
        User user = userMapper.userDTOToUser(userDTO);
        User updatedUser = userRepository.findById(id)
                .map(repoUser -> {
                    repoUser.setName(user.getName());
                    repoUser.setSurname(user.getSurname());
                    repoUser.setBirthDate(user.getBirthDate());
                    repoUser.setEmail(user.getEmail());
                    repoUser.setCards(user.getCards());
                    return userRepository.save(repoUser);
                })
                .orElseThrow(() -> new RuntimeException("User with id " + id + " does not exist"));

        return userMapper.userToUserDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with id " + id + " does not exist"));
        userRepository.delete(user);
    }
}