
package com.novawavex.novawavex.service;

import com.novawavex.novawavex.dto.ProfileNameRequest;
import com.novawavex.novawavex.dto.UserRequest;
import com.novawavex.novawavex.dto.UserResponse;
import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.exception.DuplicateResourceException;
import com.novawavex.novawavex.exception.ResourceNotFoundException;
import com.novawavex.novawavex.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        user = new User(
                "Hemant Kumar",
                "hemant@novawavex.com",
                "encodedPassword",
                "USER"
        );
    }


    // =========================================================
    // 1. CREATE USER
    // =========================================================

    @Test
    void createUser_whenEmailIsAvailable_shouldCreateUser() {

        UserRequest request = new UserRequest();

        request.setFullName(
                "Hemant Kumar"
        );

        request.setEmail(
                "hemant@novawavex.com"
        );

        request.setPassword(
                "Test12345"
        );

        when(userRepository.findByEmail(
                "hemant@novawavex.com"
        )).thenReturn(Optional.empty());

        when(passwordEncoder.encode(
                "Test12345"
        )).thenReturn("encodedPassword");

        when(userRepository.save(
                any(User.class)
        )).thenAnswer(invocation -> {

            User savedUser =
                    invocation.getArgument(0);

            savedUser.setId(1L);

            return savedUser;
        });

        UserResponse response =
                userService.createUser(request);

        assertNotNull(response);

        assertEquals(
                1L,
                response.getId()
        );

        assertEquals(
                "Hemant Kumar",
                response.getFullName()
        );

        assertEquals(
                "hemant@novawavex.com",
                response.getEmail()
        );

        assertEquals(
                "USER",
                response.getRole()
        );

        verify(passwordEncoder)
                .encode("Test12345");

        verify(userRepository)
                .save(any(User.class));
    }


    // =========================================================
    // 2. CREATE USER - DUPLICATE EMAIL
    // =========================================================

    @Test
    void createUser_whenEmailAlreadyExists_shouldThrowException() {

        UserRequest request = new UserRequest();

        request.setFullName(
                "Hemant Kumar"
        );

        request.setEmail(
                "hemant@novawavex.com"
        );

        request.setPassword(
                "Test12345"
        );

        when(userRepository.findByEmail(
                "hemant@novawavex.com"
        )).thenReturn(Optional.of(user));

        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> userService.createUser(request)
                );

        assertEquals(
                "Email already registered",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }


    // =========================================================
    // 3. PASSWORD SHOULD BE ENCODED
    // =========================================================

    @Test
    void createUser_shouldEncodePasswordBeforeSaving() {

        UserRequest request = new UserRequest();

        request.setFullName(
                "Hemant Kumar"
        );

        request.setEmail(
                "hemant@novawavex.com"
        );

        request.setPassword(
                "Test12345"
        );

        when(userRepository.findByEmail(
                "hemant@novawavex.com"
        )).thenReturn(Optional.empty());

        when(passwordEncoder.encode(
                "Test12345"
        )).thenReturn(
                "secureEncodedPassword"
        );

        when(userRepository.save(
                any(User.class)
        )).thenAnswer(invocation -> {

            User savedUser =
                    invocation.getArgument(0);

            savedUser.setId(2L);

            return savedUser;
        });

        userService.createUser(request);

        verify(passwordEncoder)
                .encode("Test12345");

        verify(userRepository)
                .save(argThat(savedUser ->
                        "secureEncodedPassword".equals(
                                savedUser.getPassword()
                        )
                ));
    }


    // =========================================================
    // 4. CREATE USER - ROLE SHOULD BE USER
    // =========================================================

    @Test
    void createUser_shouldAssignUserRole() {

        UserRequest request = new UserRequest();

        request.setFullName(
                "Normal User"
        );

        request.setEmail(
                "normal@novawavex.com"
        );

        request.setPassword(
                "Test12345"
        );

        when(userRepository.findByEmail(
                "normal@novawavex.com"
        )).thenReturn(Optional.empty());

        when(passwordEncoder.encode(
                "Test12345"
        )).thenReturn("encodedPassword");

        when(userRepository.save(
                any(User.class)
        )).thenAnswer(invocation -> {

            User savedUser =
                    invocation.getArgument(0);

            savedUser.setId(3L);

            return savedUser;
        });

        userService.createUser(request);

        verify(userRepository)
                .save(argThat(savedUser ->
                        "USER".equals(
                                savedUser.getRole()
                        )
                ));
    }


    // =========================================================
    // 5. GET ALL USERS
    // =========================================================

    @Test
    void getAllUsers_shouldReturnAllUsers() {

        User user1 =
                new User(
                        "User One",
                        "user1@novawavex.com",
                        "password1",
                        "USER"
                );

        User user2 =
                new User(
                        "User Two",
                        "user2@novawavex.com",
                        "password2",
                        "USER"
                );

        user1.setId(1L);

        user2.setId(2L);

        when(userRepository.findAll())
                .thenReturn(
                        List.of(
                                user1,
                                user2
                        )
                );

        List<UserResponse> result =
                userService.getAllUsers();

        assertNotNull(result);

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                1L,
                result.get(0).getId()
        );

        assertEquals(
                "User One",
                result.get(0).getFullName()
        );

        assertEquals(
                "user1@novawavex.com",
                result.get(0).getEmail()
        );

        assertEquals(
                2L,
                result.get(1).getId()
        );

        assertEquals(
                "User Two",
                result.get(1).getFullName()
        );

        assertEquals(
                "user2@novawavex.com",
                result.get(1).getEmail()
        );

        verify(userRepository)
                .findAll();
    }


    // =========================================================
    // 6. GET ALL USERS - EMPTY
    // =========================================================

    @Test
    void getAllUsers_whenNoUsersExist_shouldReturnEmptyList() {

        when(userRepository.findAll())
                .thenReturn(
                        List.of()
                );

        List<UserResponse> result =
                userService.getAllUsers();

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );

        verify(userRepository)
                .findAll();
    }


    // =========================================================
    // 7. GET USER BY ID
    // =========================================================

    @Test
    void getUserById_whenUserExists_shouldReturnUser() {

        user.setId(10L);

        when(userRepository.findById(10L))
                .thenReturn(
                        Optional.of(user)
                );

        UserResponse response =
                userService.getUserById(10L);

        assertNotNull(response);

        assertEquals(
                10L,
                response.getId()
        );

        assertEquals(
                "Hemant Kumar",
                response.getFullName()
        );

        assertEquals(
                "hemant@novawavex.com",
                response.getEmail()
        );

        assertEquals(
                "USER",
                response.getRole()
        );

        verify(userRepository)
                .findById(10L);
    }


    // =========================================================
    // 8. GET USER BY ID - NOT FOUND
    // =========================================================

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnNull() {

        when(userRepository.findById(999L))
                .thenReturn(
                        Optional.empty()
                );

        UserResponse response =
                userService.getUserById(999L);

        assertNull(response);

        verify(userRepository)
                .findById(999L);
    }


    // =========================================================
    // 9. GET CURRENT USER
    // =========================================================

    @Test
    void getCurrentUser_whenUserExists_shouldReturnUser() {

        user.setId(20L);

        when(userRepository.findByEmail(
                "hemant@novawavex.com"
        )).thenReturn(
                Optional.of(user)
        );

        UserResponse response =
                userService.getCurrentUser(
                        "hemant@novawavex.com"
                );

        assertNotNull(response);

        assertEquals(
                20L,
                response.getId()
        );

        assertEquals(
                "Hemant Kumar",
                response.getFullName()
        );

        assertEquals(
                "hemant@novawavex.com",
                response.getEmail()
        );

        assertEquals(
                "USER",
                response.getRole()
        );

        verify(userRepository)
                .findByEmail(
                        "hemant@novawavex.com"
                );
    }


    // =========================================================
    // 10. GET CURRENT USER - NOT FOUND
    // =========================================================

    @Test
    void getCurrentUser_whenUserDoesNotExist_shouldThrowException() {

        when(userRepository.findByEmail(
                "missing@novawavex.com"
        )).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.getCurrentUser(
                                "missing@novawavex.com"
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail(
                        "missing@novawavex.com"
                );
    }


    // =========================================================
    // 11. UPDATE CURRENT USER NAME
    // =========================================================

    @Test
    void updateCurrentUserName_shouldUpdateAndReturnUser() {

        user.setId(30L);

        ProfileNameRequest request =
                new ProfileNameRequest(
                        "  Hemant Kumar Updated  "
                );

        when(userRepository.findByEmail(
                "hemant@novawavex.com"
        )).thenReturn(
                Optional.of(user)
        );

        when(userRepository.save(
                any(User.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        UserResponse response =
                userService.updateCurrentUserName(
                        "hemant@novawavex.com",
                        request
                );

        assertNotNull(response);

        assertEquals(
                "Hemant Kumar Updated",
                response.getFullName()
        );

        assertEquals(
                "hemant@novawavex.com",
                response.getEmail()
        );

        assertEquals(
                30L,
                response.getId()
        );

        assertEquals(
                "Hemant Kumar Updated",
                user.getFullName()
        );

        verify(userRepository)
                .save(user);
    }


    // =========================================================
    // 12. UPDATE CURRENT USER NAME - USER NOT FOUND
    // =========================================================

    @Test
    void updateCurrentUserName_whenUserDoesNotExist_shouldThrowException() {

        ProfileNameRequest request =
                new ProfileNameRequest(
                        "Updated Name"
                );

        when(userRepository.findByEmail(
                "missing@novawavex.com"
        )).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.updateCurrentUserName(
                                "missing@novawavex.com",
                                request
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    // =========================================================
    // 13. UPDATE CURRENT USER NAME - BLANK
    // =========================================================

    @Test
    void updateCurrentUserName_whenNameIsBlank_shouldThrowException() {

        ProfileNameRequest request =
                new ProfileNameRequest(
                        "   "
                );

        when(userRepository.findByEmail(
                "hemant@novawavex.com"
        )).thenReturn(
                Optional.of(user)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.updateCurrentUserName(
                                "hemant@novawavex.com",
                                request
                        )
                );

        assertEquals(
                "Full name cannot be empty",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    // =========================================================
    // 14. UPDATE CURRENT USER NAME - PROFILE IMAGE
    // =========================================================

    @Test
    void updateCurrentUserName_shouldPreserveProfileImage() {

        user.setId(40L);

        user.setProfileImage(
                "data:image/png;base64,test-image"
        );

        ProfileNameRequest request =
                new ProfileNameRequest(
                        "Updated Profile Name"
                );

        when(userRepository.findByEmail(
                "hemant@novawavex.com"
        )).thenReturn(
                Optional.of(user)
        );

        when(userRepository.save(
                any(User.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        UserResponse response =
                userService.updateCurrentUserName(
                        "hemant@novawavex.com",
                        request
                );

        assertNotNull(response);

        assertEquals(
                "Updated Profile Name",
                response.getFullName()
        );

        assertEquals(
                "data:image/png;base64,test-image",
                response.getProfileImage()
        );

        verify(userRepository)
                .save(user);
    }
}
