
package com.novawavex.novawavex.security;

import com.novawavex.novawavex.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private FilterChain filterChain;

    @BeforeEach
    void setUp() {

        jwtService = mock(JwtService.class);

        jwtAuthenticationFilter =
                new JwtAuthenticationFilter(
                        jwtService
                );

        filterChain = mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    // =========================================================
    // 1. NO AUTHORIZATION HEADER
    // =========================================================

    @Test
    void doFilterInternal_whenAuthorizationHeaderMissing_shouldContinue()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(
                request,
                response
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(jwtService);
    }

    // =========================================================
    // 2. BLANK AUTHORIZATION HEADER
    // =========================================================

    @Test
    void doFilterInternal_whenAuthorizationHeaderBlank_shouldContinue()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "   "
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(
                request,
                response
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(jwtService);
    }

    // =========================================================
    // 3. INVALID AUTHORIZATION SCHEME
    // =========================================================

    @Test
    void doFilterInternal_whenAuthorizationSchemeInvalid_shouldReturn401()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Basic abc123"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        assertTrue(
                response.getContentAsString()
                        .contains(
                                "\"status\":401"
                        )
        );

        assertTrue(
                response.getContentAsString()
                        .contains(
                                "Invalid Authorization header"
                        )
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                request,
                response
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(jwtService);
    }

    // =========================================================
    // 4. EMPTY BEARER TOKEN
    // =========================================================

    @Test
    void doFilterInternal_whenBearerTokenEmpty_shouldReturn401()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Bearer "
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        assertTrue(
                response.getContentAsString()
                        .contains(
                                "JWT token is missing"
                        )
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                request,
                response
        );

        verifyNoInteractions(jwtService);
    }

    // =========================================================
    // 5. VALID JWT
    // =========================================================

    @Test
    void doFilterInternal_whenTokenValid_shouldAuthenticateUser()
            throws ServletException, IOException {

        String token =
                "valid.jwt.token";

        when(
                jwtService.extractEmail(token)
        ).thenReturn(
                "user@novawavex.com"
        );

        when(
                jwtService.extractRole(token)
        ).thenReturn(
                "USER"
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(
                request,
                response
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertEquals(
                "user@novawavex.com",
                authentication.getName()
        );

        assertTrue(
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals(
                                                        "ROLE_USER"
                                                )
                        )
        );

        verify(
                jwtService
        ).extractEmail(token);

        verify(
                jwtService
        ).extractRole(token);
    }

    // =========================================================
    // 6. VALID JWT WITH ADMIN ROLE
    // =========================================================

    @Test
    void doFilterInternal_whenAdminToken_shouldCreateAdminAuthority()
            throws ServletException, IOException {

        String token =
                "valid.admin.jwt";

        when(
                jwtService.extractEmail(token)
        ).thenReturn(
                "admin@novawavex.com"
        );

        when(
                jwtService.extractRole(token)
        ).thenReturn(
                "ADMIN"
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/admin"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertTrue(
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                        )
        );
    }

    // =========================================================
    // 7. JWT WITH MISSING EMAIL
    // =========================================================

    @Test
    void doFilterInternal_whenEmailMissing_shouldReturn401()
            throws ServletException, IOException {

        String token =
                "missing.email.jwt";

        when(
                jwtService.extractEmail(token)
        ).thenReturn(null);

        when(
                jwtService.extractRole(token)
        ).thenReturn("USER");

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        assertTrue(
                response.getContentAsString()
                        .contains(
                                "Invalid JWT token"
                        )
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                request,
                response
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    // =========================================================
    // 8. JWT WITH BLANK EMAIL
    // =========================================================

    @Test
    void doFilterInternal_whenEmailBlank_shouldReturn401()
            throws ServletException, IOException {

        String token =
                "blank.email.jwt";

        when(
                jwtService.extractEmail(token)
        ).thenReturn("   ");

        when(
                jwtService.extractRole(token)
        ).thenReturn("USER");

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                request,
                response
        );
    }

    // =========================================================
    // 9. JWT WITH MISSING ROLE
    // =========================================================

    @Test
    void doFilterInternal_whenRoleMissing_shouldReturn401()
            throws ServletException, IOException {

        String token =
                "missing.role.jwt";

        when(
                jwtService.extractEmail(token)
        ).thenReturn(
                "user@novawavex.com"
        );

        when(
                jwtService.extractRole(token)
        ).thenReturn(null);

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        assertTrue(
                response.getContentAsString()
                        .contains(
                                "Invalid JWT token"
                        )
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                request,
                response
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    // =========================================================
    // 10. JWT WITH BLANK ROLE
    // =========================================================

    @Test
    void doFilterInternal_whenRoleBlank_shouldReturn401()
            throws ServletException, IOException {

        String token =
                "blank.role.jwt";

        when(
                jwtService.extractEmail(token)
        ).thenReturn(
                "user@novawavex.com"
        );

        when(
                jwtService.extractRole(token)
        ).thenReturn("   ");

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                request,
                response
        );
    }

    // =========================================================
    // 11. INVALID JWT EXCEPTION
    // =========================================================

    @Test
    void doFilterInternal_whenJwtServiceThrowsException_shouldReturn401()
            throws ServletException, IOException {

        String token =
                "invalid.jwt";

        when(
                jwtService.extractEmail(token)
        ).thenThrow(
                new RuntimeException(
                        "Invalid token"
                )
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        assertTrue(
                response.getContentAsString()
                        .contains(
                                "Invalid or expired JWT token"
                        )
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                request,
                response
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    // =========================================================
    // 12. EXISTING AUTHENTICATION SHOULD NOT BE OVERWRITTEN
    // =========================================================

    @Test
    void doFilterInternal_whenAuthenticationAlreadyExists_shouldNotOverwrite()
            throws ServletException, IOException {

        Authentication existingAuthentication =
                new UsernamePasswordAuthenticationToken(
                        "existing@novawavex.com",
                        null
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        existingAuthentication
                );

        String token =
                "valid.jwt";

        when(
                jwtService.extractEmail(token)
        ).thenReturn(
                "jwt-user@novawavex.com"
        );

        when(
                jwtService.extractRole(token)
        ).thenReturn(
                "USER"
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertSame(
                existingAuthentication,
                authentication
        );

        assertEquals(
                "existing@novawavex.com",
                authentication.getName()
        );

        verify(filterChain).doFilter(
                request,
                response
        );
    }

    // =========================================================
    // 13. LOGIN ENDPOINT SHOULD NOT BE FILTERED
    // =========================================================

    @Test
    void shouldNotFilter_loginEndpoint_shouldReturnTrue() {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/api/auth/login"
                );

        request.setServletPath(
                "/api/auth/login"
        );

        assertTrue(
                jwtAuthenticationFilter
                        .shouldNotFilter(request)
        );
    }

    // =========================================================
    // 14. REGISTER ENDPOINT SHOULD NOT BE FILTERED
    // =========================================================

    @Test
    void shouldNotFilter_registerEndpoint_shouldReturnTrue() {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/api/auth/register"
                );

        request.setServletPath(
                "/api/auth/register"
        );

        assertTrue(
                jwtAuthenticationFilter
                        .shouldNotFilter(request)
        );
    }

    // =========================================================
    // 15. FORGOT PASSWORD ENDPOINT SHOULD NOT BE FILTERED
    // =========================================================

    @Test
    void shouldNotFilter_forgotPasswordEndpoint_shouldReturnTrue() {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/api/auth/forgot-password"
                );

        request.setServletPath(
                "/api/auth/forgot-password"
        );

        assertTrue(
                jwtAuthenticationFilter
                        .shouldNotFilter(request)
        );
    }

    // =========================================================
    // 16. RESET PASSWORD ENDPOINT SHOULD NOT BE FILTERED
    // =========================================================

    @Test
    void shouldNotFilter_resetPasswordEndpoint_shouldReturnTrue() {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/api/auth/reset-password"
                );

        request.setServletPath(
                "/api/auth/reset-password"
        );

        assertTrue(
                jwtAuthenticationFilter
                        .shouldNotFilter(request)
        );
    }

    // =========================================================
    // 17. PROTECTED ENDPOINT SHOULD BE FILTERED
    // =========================================================

    @Test
    void shouldNotFilter_protectedEndpoint_shouldReturnFalse() {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.setServletPath(
                "/api/workflows"
        );

        assertFalse(
                jwtAuthenticationFilter
                        .shouldNotFilter(request)
        );
    }

    // =========================================================
    // 18. UNAUTHORIZED RESPONSE CONTENT TYPE
    // =========================================================

    @Test
    void unauthorizedResponse_shouldHaveJsonContentType()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Basic invalid"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertEquals(
                "application/json",
                response.getContentType()
        );

        assertEquals(
                "UTF-8",
                response.getCharacterEncoding()
        );
    }

    // =========================================================
    // 19. UNAUTHORIZED RESPONSE SHOULD CONTAIN PATH
    // =========================================================

    @Test
    void unauthorizedResponse_shouldContainRequestPath()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/protected/test"
                );

        request.addHeader(
                "Authorization",
                "Basic invalid"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        String body =
                response.getContentAsString();

        assertTrue(
                body.contains(
                        "\"path\":\"/api/protected/test\""
                )
        );
    }

    // =========================================================
    // 20. UNAUTHORIZED RESPONSE SHOULD CONTAIN ERROR
    // =========================================================

    @Test
    void unauthorizedResponse_shouldContainError()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Basic invalid"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        String body =
                response.getContentAsString();

        assertTrue(
                body.contains(
                        "\"error\":\"Unauthorized\""
                )
        );
    }

    // =========================================================
    // 21. INVALID TOKEN SHOULD CLEAR SECURITY CONTEXT
    // =========================================================

    @Test
    void doFilterInternal_whenInvalidToken_shouldClearSecurityContext()
            throws ServletException, IOException {

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "old-user@novawavex.com",
                                null
                        )
                );

        String token =
                "invalid.jwt";

        when(
                jwtService.extractEmail(token)
        ).thenThrow(
                new RuntimeException(
                        "Invalid token"
                )
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    // =========================================================
    // 22. INVALID AUTHORIZATION HEADER SHOULD CLEAR CONTEXT
    // =========================================================

    @Test
    void doFilterInternal_whenInvalidAuthorizationHeader_shouldClearSecurityContext()
            throws ServletException, IOException {

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "old-user@novawavex.com",
                                null
                        )
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/workflows"
                );

        request.addHeader(
                "Authorization",
                "Basic invalid"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }
}
