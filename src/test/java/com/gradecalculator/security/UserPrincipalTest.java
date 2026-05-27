package com.gradecalculator.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserPrincipal.
 */
class UserPrincipalTest {

    @Test
    void createsUserPrincipalWithAllFields() {
        UserPrincipal principal = new UserPrincipal(
                1L, "admin", "Admin User", "admin@college.edu", "Computer Science", "ADMIN"
        );

        assertThat(principal.getId()).isEqualTo(1L);
        assertThat(principal.getUsername()).isEqualTo("admin");
        assertThat(principal.getName()).isEqualTo("Admin User");
        assertThat(principal.getEmail()).isEqualTo("admin@college.edu");
        assertThat(principal.getDepartment()).isEqualTo("Computer Science");
        assertThat(principal.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void getAuthoritiesReturnsCorrectAuthorityForAdmin() {
        UserPrincipal principal = new UserPrincipal(
                1L, "admin", "Admin", null, null, "ADMIN"
        );

        var authorities = principal.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities).contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void getAuthoritiesReturnsCorrectAuthorityForFaculty() {
        UserPrincipal principal = new UserPrincipal(
                2L, "prof", "Professor", null, null, "FACULTY"
        );

        var authorities = principal.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities).contains(new SimpleGrantedAuthority("ROLE_FACULTY"));
    }

    @Test
    void getAuthoritiesReturnsCorrectAuthorityForStudent() {
        UserPrincipal principal = new UserPrincipal(
                3L, "student", "Student", null, null, "STUDENT"
        );

        var authorities = principal.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities).contains(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    @Test
    void isAccountNonExpiredReturnsTrue() {
        UserPrincipal principal = new UserPrincipal(
                1L, "user", "User", null, null, "STUDENT"
        );

        assertThat(principal.isAccountNonExpired()).isTrue();
    }

    @Test
    void isAccountNonLockedReturnsTrue() {
        UserPrincipal principal = new UserPrincipal(
                1L, "user", "User", null, null, "STUDENT"
        );

        assertThat(principal.isAccountNonLocked()).isTrue();
    }

    @Test
    void isCredentialsNonExpiredReturnsTrue() {
        UserPrincipal principal = new UserPrincipal(
                1L, "user", "User", null, null, "STUDENT"
        );

        assertThat(principal.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void isEnabledReturnsTrue() {
        UserPrincipal principal = new UserPrincipal(
                1L, "user", "User", null, null, "STUDENT"
        );

        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    void getAuthoritiesIsNotModifiable() {
        UserPrincipal principal = new UserPrincipal(
                1L, "user", "User", null, null, "STUDENT"
        );

        var authorities = principal.getAuthorities();
        
        assertThat(authorities).isUnmodifiable();
    }

    @Test
    void authoritiesContainsOnlyRoleAuthority() {
        UserPrincipal principal = new UserPrincipal(
                1L, "test", "Test", null, null, "FACULTY"
        );

        var authorities = principal.getAuthorities();

        // Should only have the role authority
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_FACULTY");
    }

    @Test
    void roleIsCapitalized() {
        UserPrincipal admin = new UserPrincipal(1L, "a", "A", null, null, "admin");
        UserPrincipal faculty = new UserPrincipal(2L, "f", "F", null, null, "faculty");
        UserPrincipal student = new UserPrincipal(3L, "s", "S", null, null, "student");

        assertThat(admin.getAuthorities())
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        assertThat(faculty.getAuthorities())
            .anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));
        assertThat(student.getAuthorities())
            .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
    }
}
