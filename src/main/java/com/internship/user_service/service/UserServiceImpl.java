package com.internship.user_service.service;

import com.internship.user_service.dto.UserDTO;
import com.internship.user_service.exception.custom_exceptions.UserNotFoundException;
import com.internship.user_service.mapper.UserMapper;
import com.internship.user_service.model.User;
import com.internship.user_service.repository.UserRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    private final CacheManager cacheManager;

    public UserServiceImpl(UserRepository userRepository,  UserMapper userMapper, CacheManager cacheManager) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional
    @Caching(put = {
            @CachePut(cacheNames = "userById", key = "#result.id"),
            @CachePut(cacheNames = "userByEmail", key = "#result.email")
    })
    public UserDTO createUser(UserDTO userDTO){
        User user = userRepository.save(userMapper.userDTOToUser(userDTO));

        return userMapper.userToUserDTO(user);
    }

    @Override
    @Cacheable(cacheNames = "userById", key = "#id")
    public UserDTO getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " does not exist"));

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
    @Cacheable(cacheNames = "userByEmail", key = "#email")
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " does not exist"));

        return userMapper.userToUserDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(UUID id, UserDTO userDTO) {
        // fetch old email for eviction if changed
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " does not exist"));
        String oldEmail = existing.getEmail();

        User user = userMapper.userDTOToUser(userDTO);
        User updatedUser = userFromRepository(id, user);
        UserDTO updatedDto = userMapper.userToUserDTO(updatedUser);

        // update id cache
        Cache idCache = cacheManager.getCache("userById");
        if (idCache != null) {
            idCache.put(id, updatedDto);
        }

        // manage email cache
        Cache emailCache = cacheManager.getCache("userByEmail");
        if (emailCache != null) {
            if (oldEmail != null && !oldEmail.equals(updatedDto.getEmail())) {
                emailCache.evict(oldEmail);
            }
            emailCache.put(updatedDto.getEmail(), updatedDto);
        }

        return updatedDto;
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " does not exist"));
        userRepository.delete(user);

        // evict caches
        Cache idCache = cacheManager.getCache("userById");
        if (idCache != null) {
            idCache.evict(id);
        }
        Cache emailCache = cacheManager.getCache("userByEmail");
        if (emailCache != null && user.getEmail() != null) {
            emailCache.evict(user.getEmail());
        }
    }

    private User userFromRepository(UUID id, User user){
        return userRepository.findById(id)
                .map(repoUser -> {
                    repoUser.setName(user.getName());
                    repoUser.setSurname(user.getSurname());
                    repoUser.setBirthDate(user.getBirthDate());
                    repoUser.setEmail(user.getEmail());
                    repoUser.setCards(user.getCards());
                    return userRepository.save(repoUser);
                })
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " does not exist"));
    }
}