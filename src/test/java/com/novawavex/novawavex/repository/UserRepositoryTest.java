package com.novawavex.novawavex.repository;

import com.novawavex.novawavex.entity.User;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;


    // =========================================================
    // 1. SAVE USER
    // =========================================================

    @Test
    void saveUser_shouldPersistUser() {

        User user = new User(
                "Repository Save Test User",
                "repo-save-test@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        assertNotNull(savedUser.getId());

        assertEquals(
                "Repository Save Test User",
                savedUser.getFullName()
        );

        assertEquals(
                "repo-save-test@novawavex.com",
                savedUser.getEmail()
        );

        assertEquals(
                "USER",
                savedUser.getRole()
        );
    }


    // =========================================================
    // 2. FIND USER BY EMAIL
    // =========================================================

    @Test
    void findByEmail_whenUserExists_shouldReturnUser() {

        User user = new User(
                "Repository Find Test User",
                "repo-find-test@novawavex.com",
                "encodedPassword",
                "USER"
        );

        userRepository.save(user);

        Optional<User> result =
                userRepository.findByEmail(
                        "repo-find-test@novawavex.com"
                );

        assertTrue(result.isPresent());

        assertEquals(
                "Repository Find Test User",
                result.get().getFullName()
        );

        assertEquals(
                "repo-find-test@novawavex.com",
                result.get().getEmail()
        );
    }


    // =========================================================
    // 3. FIND USER BY EMAIL - NOT FOUND
    // =========================================================

    @Test
    void findByEmail_whenUserDoesNotExist_shouldReturnEmpty() {

        Optional<User> result =
                userRepository.findByEmail(
                        "repo-missing-test@novawavex.com"
                );

        assertTrue(result.isEmpty());
    }


    // =========================================================
    // 4. FIND USER BY ID
    // =========================================================

    @Test
    void findById_whenUserExists_shouldReturnUser() {

        User user = new User(
                "Repository ID Test User",
                "repo-id-test@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        Optional<User> result =
                userRepository.findById(
                        savedUser.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                savedUser.getId(),
                result.get().getId()
        );

        assertEquals(
                "repo-id-test@novawavex.com",
                result.get().getEmail()
        );
    }


    // =========================================================
    // 5. DELETE USER
    // =========================================================

    @Test
    void deleteUser_shouldRemoveUser() {

        User user = new User(
                "Repository Delete Test User",
                "repo-delete-test@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        Long userId =
                savedUser.getId();

        userRepository.delete(savedUser);

        Optional<User> result =
                userRepository.findById(userId);

        assertTrue(result.isEmpty());
    }

}