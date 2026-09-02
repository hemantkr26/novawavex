
package com.novawavex.novawavex;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.novawavex.novawavex.security.JwtAuthenticationFilter;
import com.novawavex.novawavex.service.AuthService;
import com.novawavex.novawavex.service.JwtService;
import com.novawavex.novawavex.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class NovawavexApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        assertNotNull(authService);
        assertNotNull(jwtService);
        assertNotNull(userService);
        assertNotNull(jwtAuthenticationFilter);
        assertNotNull(passwordEncoder);
    }
}

