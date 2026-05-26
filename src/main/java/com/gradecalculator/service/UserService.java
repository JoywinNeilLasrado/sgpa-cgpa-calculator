package com.gradecalculator.service;
import com.gradecalculator.util.ValidationUtil;

import com.gradecalculator.dto.request.RegisterRequest;
import com.gradecalculator.model.AppUser;
import com.gradecalculator.repository.AppUserRepository;
import com.gradecalculator.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * AppUser service for authentication
 */
@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserService.class);

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Registers a new AppUser account in the database after validating that the username is unique.
     * Sets the account state to require a password change on first login.
     *
     * @param request the registration details containing username, password, role, and optional details
     * @return the saved AppUser entity
     */
    public AppUser register(RegisterRequest request) {
        ValidationUtil.requireNonNull(request, "Register request cannot be null");
        ValidationUtil.requireNonBlank(request.getUsername(), "Username cannot be null or empty");
        ValidationUtil.requireNonBlank(request.getPassword(), "Password cannot be null or empty");
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setName(request.getName() != null && !request.getName().trim().isEmpty() ? request.getName() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setDepartment(request.getDepartment());
        user.setMustChangePassword(true);

        return userRepository.save(user);
    }

    /**
     * Authenticates a user by validating their username and password using the authentication manager,
     * and generates a valid JWT token upon success.
     *
     * @param username the username of the user to authenticate
     * @param password the raw password of the user
     * @return a signed, base64-encoded JWT token containing user identity and role
     */
    public String authenticate(String username, String password) {
        ValidationUtil.requireNonBlank(username, "Username cannot be null or empty");
        ValidationUtil.requireNonBlank(password, "Password cannot be null or empty");

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );

        return jwtTokenProvider.generateToken(authentication);
    }

    /**
     * Resolves a user from the repository by their unique username string.
     *
     * @param username the username to lookup
     * @return an Optional containing the AppUser if found
     */
    public Optional<AppUser> findByUsername(String username) {
        ValidationUtil.requireNonBlank(username, "Username cannot be null or empty");
        return userRepository.findByUsername(username);
    }

    /**
     * Resolves a user from the repository by their primary key identifier.
     *
     * @param id the primary key of the user
     * @return an Optional containing the AppUser if found
     */
    public Optional<AppUser> findById(Long id) {
        ValidationUtil.requireNonNull(id, "User ID cannot be null");
        return userRepository.findById(id);
    }

    /**
     * Updates/changes the logged-in user's password after verifying their old password.
     * Validates that the new password meets security requirements (e.g. minimum length).
     *
     * @param userId      the primary key of the user changing their password
     * @param oldPassword the raw current password of the user
     * @param newPassword the raw new password of the user
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        ValidationUtil.requireNonNull(userId, "AppUser ID cannot be null");
        ValidationUtil.requireNonBlank(oldPassword, "Old password cannot be null or empty");
        ValidationUtil.requireNonBlank(newPassword, "New password cannot be null or empty");
        validatePassword(newPassword);
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("AppUser not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
        logger.info("Security Event: User ID: '{}' changed their password successfully", userId);
    }

    /**
     * Administrative password reset mechanism. Resets a user's password directly by user ID.
     * Sets the account state to require a password change on their next login.
     *
     * @param userId      the primary key of the target user
     * @param newPassword the new password to set
     */
    public void adminChangePassword(Long userId, String newPassword) {
        ValidationUtil.requireNonNull(userId, "AppUser ID cannot be null");
        ValidationUtil.requireNonBlank(newPassword, "New password cannot be null or empty");
        validatePassword(newPassword);
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("AppUser not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);
        logger.warn("Security Event: Admin changed/reset password for user ID: '{}'", userId);
    }

    /**
     * Administrative password reset mechanism. Resets a user's password directly by username string.
     * Sets the account state to require a password change on their next login.
     *
     * @param username    the username of the target user
     * @param newPassword the new password to set
     */
    public void adminChangePasswordByUsername(String username, String newPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be null or empty");
        }
        validatePassword(newPassword);
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("AppUser not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);
        logger.warn("Security Event: Admin changed/reset password for username: '{}'", username);
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }
}