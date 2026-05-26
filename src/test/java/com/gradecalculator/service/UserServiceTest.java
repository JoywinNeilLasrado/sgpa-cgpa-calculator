package com.gradecalculator.service;

import com.gradecalculator.dto.request.RegisterRequest;
import com.gradecalculator.model.AppUser;
import com.gradecalculator.repository.AppUserRepository;
import com.gradecalculator.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private AppUserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, passwordEncoder, authenticationManager, jwtTokenProvider);
    }

    @Test
    void registerThrowsWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user1");
        
        when(userRepository.existsByUsername("user1")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void registerSavesUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user1");
        request.setPassword("Password123!");
        request.setRole(AppUser.Role.STUDENT);
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setDepartment("CS");

        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        
        AppUser savedUser = new AppUser(1L, "user1", "encodedPassword", AppUser.Role.STUDENT, "John Doe", "john@example.com", "CS");
        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

        AppUser result = service.register(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("user1");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getRole()).isEqualTo(AppUser.Role.STUDENT);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getDepartment()).isEqualTo("CS");

        verify(userRepository).save(argThat(user -> 
            user.getUsername().equals("user1") &&
            user.getPassword().equals("encodedPassword") &&
            user.getName().equals("John Doe") &&
            user.getEmail().equals("john@example.com") &&
            user.getDepartment().equals("CS")
        ));
    }

    @Test
    void registerDefaultsNameToUsernameWhenEmpty() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user1");
        request.setPassword("Password123!");
        request.setRole(AppUser.Role.STUDENT);
        request.setName("   ");

        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded");
        
        when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        AppUser result = service.register(request);

        assertThat(result.getName()).isEqualTo("user1");
    }

    @Test
    void authenticateReturnsToken() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("mockedToken");

        String result = service.authenticate("user1", "pass1");

        assertThat(result).isEqualTo("mockedToken");
        verify(authenticationManager).authenticate(argThat(token -> 
            token.getPrincipal().equals("user1") && token.getCredentials().equals("pass1")
        ));
    }

    @Test
    void findByUsernameReturnsUser() {
        AppUser user = new AppUser();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        Optional<AppUser> result = service.findByUsername("user1");

        assertThat(result).contains(user);
    }

    @Test
    void findByIdReturnsUser() {
        AppUser user = new AppUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<AppUser> result = service.findById(1L);

        assertThat(result).contains(user);
    }

    @Test
    void changePasswordThrowsWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changePassword(1L, "old", "newPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("AppUser not found");
    }

    @Test
    void changePasswordThrowsWhenOldPasswordDoesNotMatch() {
        AppUser user = new AppUser();
        user.setPassword("correctOldEncoded");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "correctOldEncoded")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(1L, "wrongOld", "newPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid old password");
    }

    @Test
    void changePasswordSavesNewPasswordSuccessfully() {
        AppUser user = new AppUser();
        user.setPassword("oldEncoded");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "oldEncoded")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncoded");

        service.changePassword(1L, "old", "newPassword");

        assertThat(user.getPassword()).isEqualTo("newEncoded");
        verify(userRepository).save(user);
    }

    @Test
    void adminChangePasswordChangesPasswordDirectly() {
        AppUser user = new AppUser();
        user.setPassword("oldEncoded");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncoded");

        service.adminChangePassword(1L, "newPassword");

        assertThat(user.getPassword()).isEqualTo("newEncoded");
        verify(userRepository).save(user);
    }

    @Test
    void adminChangePasswordByUsernameChangesPasswordDirectly() {
        AppUser user = new AppUser();
        user.setPassword("oldEncoded");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncoded");

        service.adminChangePasswordByUsername("user1", "newPassword");

        assertThat(user.getPassword()).isEqualTo("newEncoded");
        verify(userRepository).save(user);
    }
}
