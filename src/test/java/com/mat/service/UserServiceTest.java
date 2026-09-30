package com.mat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mat.entity.Base;
import com.mat.entity.Role;
import com.mat.entity.User;
import com.mat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private Base testBase;

    @BeforeEach
    void setUp() {
        testBase = new Base("Bengaluru Base", "Bengaluru");
        testBase.setId(1L);
    }

    @Test
    void testSaveAdminWithoutBaseAllowed() {
        User admin = new User("adminUser", "admin@mat.com", "plainPass", Role.ADMIN, null);
        when(passwordEncoder.encode("plainPass")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        User saved = userService.saveUser(admin);

        assertNotNull(saved);
        assertEquals(10L, saved.getId());
        assertNull(saved.getAssignedBase());
        assertEquals(Role.ADMIN, saved.getRole());
        assertEquals("$2a$10$encodedHash", saved.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testSaveAdminWithBaseAllowed() {
        User admin = new User("adminWithBase", "adminbase@mat.com", "plainPass", Role.ADMIN, testBase);
        when(passwordEncoder.encode("plainPass")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(11L);
            return u;
        });

        User saved = userService.saveUser(admin);

        assertNotNull(saved);
        assertEquals(11L, saved.getId());
        assertNotNull(saved.getAssignedBase());
        assertEquals("Bengaluru Base", saved.getAssignedBase().getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testSaveBaseCommanderWithoutBaseRejected() {
        User commander = new User("commanderUser", "cmd@mat.com", "plainPass", Role.BASE_COMMANDER, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.saveUser(commander));

        assertEquals("Assigned base is required for Base Commander.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testSaveBaseCommanderWithBaseAllowed() {
        User commander = new User("commanderUser", "cmd@mat.com", "plainPass", Role.BASE_COMMANDER, testBase);
        when(passwordEncoder.encode("plainPass")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(20L);
            return u;
        });

        User saved = userService.saveUser(commander);

        assertNotNull(saved);
        assertEquals(20L, saved.getId());
        assertNotNull(saved.getAssignedBase());
        assertEquals("Bengaluru Base", saved.getAssignedBase().getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testSaveLogisticsOfficerWithoutBaseRejected() {
        User officer = new User("logisticsUser", "log@mat.com", "plainPass", Role.LOGISTICS_OFFICER, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.saveUser(officer));

        assertEquals("Assigned base is required for Logistics Officer.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testSaveLogisticsOfficerWithBaseAllowed() {
        User officer = new User("logisticsUser", "log@mat.com", "plainPass", Role.LOGISTICS_OFFICER, testBase);
        when(passwordEncoder.encode("plainPass")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(30L);
            return u;
        });

        User saved = userService.saveUser(officer);

        assertNotNull(saved);
        assertEquals(30L, saved.getId());
        assertNotNull(saved.getAssignedBase());
        assertEquals("Bengaluru Base", saved.getAssignedBase().getName());
        verify(userRepository).save(any(User.class));
    }
}
