package com.smartagriculture.service;

import com.smartagriculture.dto.*;
import com.smartagriculture.entity.User;
import com.smartagriculture.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Public user registration - always assigns ROLE_USER, never ROLE_ADMIN
     */
    @Transactional
    public UserDto registerUser(SignupRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirmation password do not match.");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException("An account with email '" + email + "' already exists. Please sign in instead.");
        }

        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        // Secure password hashing with BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        user.setPasswordHash(hashedPassword);
        user.setRole("ROLE_USER"); // Public signup is strictly assigned ROLE_USER
        user.setPreferredLanguage(request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "en");

        User saved = userRepository.save(user);
        return new UserDto(saved);
    }

    /**
     * Authenticates user, establishes server session, and issues HttpOnly session cookie
     */
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String email = request.getEmail().trim().toLowerCase();

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Persist authentication in HTTP Session for cookie-based state
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("User account not found."));

        return new AuthResponse(user, "Login successful.");
    }

    /**
     * Invalidates current user session and clears authentication cookies
     */
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        SecurityContextHolder.clearContext();
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear JSESSIONID cookie
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        httpResponse.addCookie(cookie);
    }

    /**
     * Retrieves currently authenticated user entity
     */
    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }

        String email = auth.getName();
        return userRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Secure one-time initial administrator account setup.
     * Permanently locked if any administrator already exists.
     */
    @Transactional
    public UserDto setupInitialAdmin(AdminSetupRequest request) {
        long adminCount = userRepository.countByRole("ROLE_ADMIN");
        if (adminCount > 0) {
            throw new IllegalStateException("Initial administrator setup is permanently locked. An administrator account already exists.");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException("An account with email '" + email + "' already exists.");
        }

        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Administrator password must be at least 8 characters long.");
        }

        User admin = new User();
        admin.setFullName(request.getFullName().trim());
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setRole("ROLE_ADMIN");
        admin.setPreferredLanguage("en");

        User saved = userRepository.save(admin);
        return new UserDto(saved);
    }

    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) {
            super(message);
        }
    }
}
