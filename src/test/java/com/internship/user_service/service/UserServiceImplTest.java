package com.internship.user_service.service;

import com.internship.user_service.dto.UserDTO;
import com.internship.user_service.exception.custom_exceptions.UserNotFoundException;
import com.internship.user_service.mapper.UserMapper;
import com.internship.user_service.model.User;
import com.internship.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache idCache;

    @Mock
    private Cache emailCache;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;
    private UUID testUserId;
    private String testName;
    private String testSurname;
    LocalDate testBirthDate;
    private String testEmail;

    @BeforeEach
    void setUp() {
        testUserId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        testName = "Slava";
        testSurname = "Kpss";
        testBirthDate = LocalDate.of(1990, 5, 9);
        testEmail = "slavakpss@mail.com";

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setName(testName);
        testUser.setSurname(testSurname);
        testUser.setBirthDate(testBirthDate);
        testUser.setEmail(testEmail);

        testUserDTO = new UserDTO();
        testUserDTO.setId(testUserId);
        testUserDTO.setName(testName);
        testUserDTO.setSurname(testSurname);
        testUserDTO.setBirthDate(testBirthDate);
        testUserDTO.setEmail(testEmail);
    }

    @Test
    void createUser_ShouldReturnUserDTO_WhenValidInput() {
        UserDTO inputDTO = new UserDTO();
        inputDTO.setName(testName);
        inputDTO.setSurname(testSurname);
        inputDTO.setBirthDate(testBirthDate);
        inputDTO.setEmail(testEmail);

        when(userMapper.userDTOToUser(inputDTO)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        UserDTO result = userService.createUser(inputDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testName);
        assertThat(result.getEmail()).isEqualTo(testEmail);
        
        verify(userRepository).save(testUser);
        verify(userMapper).userDTOToUser(inputDTO);
        verify(userMapper).userToUserDTO(testUser);
    }

    @Test
    void getUser_ShouldReturnUserDTO_WhenUserExists() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        UserDTO result = userService.getUser(testUserId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        assertThat(result.getEmail()).isEqualTo(testEmail);
        
        verify(userRepository).findById(testUserId);
        verify(userMapper).userToUserDTO(testUser);
    }

    @Test
    void getUsers_ShouldReturnListOfUserDTOs_WhenUsersExist() {
        UUID secondUserId = UUID.randomUUID();
        List<UUID> userIds = List.of(testUserId, secondUserId);
        
        User secondUser = new User();
        secondUser.setId(secondUserId);
        secondUser.setName("Lisa");
        secondUser.setSurname("Ooes");
        secondUser.setBirthDate(LocalDate.of(1998, 8, 9));
        secondUser.setEmail("ooes@mail.com");
        
        UserDTO secondUserDTO = new UserDTO();
        secondUserDTO.setId(secondUserId);
        secondUserDTO.setName("Lisa");
        secondUserDTO.setSurname("Ooes");
        secondUserDTO.setBirthDate(LocalDate.of(1998, 8, 9));
        secondUserDTO.setEmail("ooes@mail.com");

        List<User> users = List.of(testUser, secondUser);

        when(userRepository.findAllById(userIds)).thenReturn(users);
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);
        when(userMapper.userToUserDTO(secondUser)).thenReturn(secondUserDTO);

        List<UserDTO> result = userService.getUsers(userIds);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(testUserId);
        assertThat(result.get(1).getId()).isEqualTo(secondUserId);
        
        verify(userRepository).findAllById(userIds);
    }

    @Test
    void getUsers_ShouldReturnEmptyList_WhenNoUsersExist() {
        List<UUID> userIds = List.of(testUserId);

        when(userRepository.findAllById(userIds)).thenReturn(List.of());

        List<UserDTO> result = userService.getUsers(userIds);

        assertThat(result).isEmpty();
        
        verify(userRepository).findAllById(userIds);
    }

    @Test
    void getUserByEmail_ShouldReturnUserDTO_WhenUserExists() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        UserDTO result = userService.getUserByEmail(testEmail);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(testEmail);
        assertThat(result.getId()).isEqualTo(testUserId);
        
        verify(userRepository).findByEmail(testEmail);
        verify(userMapper).userToUserDTO(testUser);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUserDTO_WhenUserExists() {
        String newName = "Updated Slava";
        String newSurname = "Updated Kpss";
        String newEmail = "new@mail.com";

        UserDTO updateDTO = new UserDTO();
        updateDTO.setName(newName);
        updateDTO.setSurname(newSurname);
        updateDTO.setBirthDate(testBirthDate);
        updateDTO.setEmail(newEmail);

        User mappedUser = new User();
        mappedUser.setName(newName);
        mappedUser.setSurname(newSurname);
        mappedUser.setBirthDate(testBirthDate);
        mappedUser.setEmail(newEmail);

        User updatedUser = new User();
        updatedUser.setId(testUserId);
        updatedUser.setName(newName);
        updatedUser.setSurname(newSurname);
        updatedUser.setBirthDate(testBirthDate);
        updatedUser.setEmail(newEmail);

        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setId(testUserId);
        updatedDTO.setName(newName);
        updatedDTO.setSurname(newSurname);
        updatedDTO.setBirthDate(testBirthDate);
        updatedDTO.setEmail(newEmail);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser), Optional.of(testUser));
        when(userMapper.userDTOToUser(updateDTO)).thenReturn(mappedUser);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.userToUserDTO(updatedUser)).thenReturn(updatedDTO);
        when(cacheManager.getCache("userById")).thenReturn(idCache);
        when(cacheManager.getCache("userByEmail")).thenReturn(emailCache);

        UserDTO result = userService.updateUser(testUserId, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getEmail()).isEqualTo(newEmail);
        
        verify(idCache).put(testUserId, updatedDTO);
        verify(emailCache).evict(testEmail);
        verify(emailCache).put(newEmail, updatedDTO);
    }

    @Test
    void deleteUser_ShouldDeleteUserAndEvictCaches_WhenUserExists() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cacheManager.getCache("userById")).thenReturn(idCache);
        when(cacheManager.getCache("userByEmail")).thenReturn(emailCache);

        userService.deleteUser(testUserId);

        verify(userRepository).findById(testUserId);
        verify(userRepository).delete(testUser);
        verify(idCache).evict(testUserId);
        verify(emailCache).evict(testEmail);
    }

    @Test
    void deleteUser_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(testUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id " + testUserId + " does not exist");

        verify(userRepository).findById(testUserId);
        verify(userRepository, never()).delete(any());
    }
}