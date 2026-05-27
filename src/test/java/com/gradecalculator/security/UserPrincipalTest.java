package com.gradecalculator.security;

import com.gradecalculator.model.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserPrincipal.
 */
class UserPrincipalTest {

    @Test
    void createsUserPrincipalWithAllFields() {
        AppUser user = new AppUser(
                1L, "admin", "password123", AppUser.Role.ADMIN, "Admin User", "admin@college.edu", "Computer Science"
        );
        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.getId()).isEqualTo(1L);
        assertThat(principal.getUsername()).isEqualTo("admin");
        assertThat(principal.getPassword()).isEqualTo("password123");
        assertThat(principal.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void getAuthoritiesReturnsCorrectAuthorityForAdmin() {
        AppUser user = new AppUser(
                1L, "admin", "password123", AppUser.Role.ADMIN, "Admin", null, null
        );
        UserPrincipal principal = new UserPrincipal(user);

        var authorities = principal.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void getAuthoritiesReturnsCorrectAuthorityForFaculty() {
        AppUser user = new AppUser(
                2L, "prof", "password123", AppUser.Role.FACULTY, "Professor", null, null
        );
        UserPrincipal principal = new UserPrincipal(user);

        var authorities = principal.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_FACULTY");
    }

    @Test
    void getAuthoritiesReturnsCorrectAuthorityForStudent() {
        AppUser user = new AppUser(
                3L, "student", "password123", AppUser.Role.STUDENT, "Student", null, null
        );
        UserPrincipal principal = new UserPrincipal(user);

        var authorities = principal.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_STUDENT");
    }

    @Test
    void isAccountNonExpiredReturnsTrue() {
        AppUser user = new AppUser(
                1L, "user", "password123", AppUser.Role.STUDENT, "User", null, null
        );
        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.isAccountNonExpired()).isTrue();
    }

    @Test
    void isAccountNonLockedReturnsTrue() {
        AppUser user = new AppUser(
                1L, "user", "password123", AppUser.Role.STUDENT, "User", null, null
        );
        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.isAccountNonLocked()).isTrue();
    }

    @Test
    void isCredentialsNonExpiredReturnsTrue() {
        AppUser user = new AppUser(
                1L, "user", "password123", AppUser.Role.STUDENT, "User", null, null
        );
        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void isEnabledReturnsTrue() {
        AppUser user = new AppUser(
                1L, "user", "password123", AppUser.Role.STUDENT, "User", null, null
        );
        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    void getAuthoritiesIsNotModifiable() {
        AppUser user = new AppUser(
                1L, "user", "password123", AppUser.Role.STUDENT, "User", null, null
        );
        UserPrincipal principal = new UserPrincipal(user);

        var authorities = principal.getAuthorities();
        
        assertThat(authorities).isUnmodifiable();
    }

    @Test
    void authoritiesContainsOnlyRoleAuthority() {
        AppUser user = new AppUser(
                1L, "test", "password123", AppUser.Role.FACULTY, "Test", null, null
        );
        UserPrincipal principal = new UserPrincipal(user);

        var authorities = principal.getAuthorities();

        // Should only have the role authority
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_FACULTY");
    }

    @Test
    void roleIsCapitalized() {
        AppUser userAdmin = new AppUser(1L, "a", "password123", AppUser.Role.ADMIN, "A", null, null);
        AppUser userFaculty = new AppUser(2L, "f", "password123", AppUser.Role.FACULTY, "F", null, null);
        AppUser userStudent = new AppUser(3L, "s", "password123", AppUser.Role.STUDENT, "S", null, null);

        UserPrincipal admin = new UserPrincipal(userAdmin);
        UserPrincipal faculty = new UserPrincipal(userFaculty);
        UserPrincipal student = new UserPrincipal(userStudent);

        assertThat(admin.getAuthorities())
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        assertThat(faculty.getAuthorities())
            .anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));
        assertThat(student.getAuthorities())
            .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
    }
}
