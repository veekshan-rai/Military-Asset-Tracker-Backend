package com.mat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mat.dto.DashboardResponse;
import com.mat.entity.Base;
import com.mat.entity.Role;
import com.mat.entity.User;
import com.mat.repository.UserRepository;
import com.mat.service.DashboardService;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardController dashboardController;

    private Base baseBengaluru;
    private Base baseMangalore;
    private User baseCommanderUser;
    private User logisticsOfficerUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        baseBengaluru = new Base("Bengaluru Base", "Bengaluru");
        baseBengaluru.setId(1L);

        baseMangalore = new Base("Mangalore Base", "Mangalore");
        baseMangalore.setId(2L);

        baseCommanderUser = new User("commander01", "cmd@mat.com", "pass", Role.BASE_COMMANDER, baseBengaluru);
        baseCommanderUser.setId(10L);

        logisticsOfficerUser = new User("logistics01", "logistics@mat.com", "pass", Role.LOGISTICS_OFFICER, baseBengaluru);
        logisticsOfficerUser.setId(20L);

        adminUser = new User("admin01", "admin@mat.com", "pass", Role.ADMIN, null);
        adminUser.setId(30L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(String username, Long userId, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username,
                userId,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testAdminCanRequestAllBases() {
        setSecurityContext("admin01", 30L, "ADMIN");
        DashboardResponse mockResponse = new DashboardResponse();
        when(dashboardService.getDashboard(isNull(), isNull(), isNull())).thenReturn(mockResponse);

        ResponseEntity<DashboardResponse> result = dashboardController.getDashboard(null, null, null);

        assertNotNull(result.getBody());
        verify(dashboardService).getDashboard(isNull(), isNull(), isNull());
    }

    @Test
    void testAdminCanRequestSpecificBase() {
        setSecurityContext("admin01", 30L, "ADMIN");
        DashboardResponse mockResponse = new DashboardResponse();
        when(dashboardService.getDashboard(eq(2L), isNull(), isNull())).thenReturn(mockResponse);

        ResponseEntity<DashboardResponse> result = dashboardController.getDashboard(2L, null, null);

        assertNotNull(result.getBody());
        verify(dashboardService).getDashboard(eq(2L), isNull(), isNull());
    }

    @Test
    void testBaseCommanderIsRestrictedToAssignedBaseWhenRequestingAllBases() {
        setSecurityContext("commander01", 10L, "BASE_COMMANDER");
        when(userRepository.findByUsername("commander01")).thenReturn(Optional.of(baseCommanderUser));
        DashboardResponse mockResponse = new DashboardResponse();
        when(dashboardService.getDashboard(eq(1L), isNull(), isNull())).thenReturn(mockResponse);

        ResponseEntity<DashboardResponse> result = dashboardController.getDashboard(null, null, null);

        assertNotNull(result.getBody());
        verify(dashboardService).getDashboard(eq(1L), isNull(), isNull());
    }

    @Test
    void testBaseCommanderCannotBypassToAnotherBase() {
        setSecurityContext("commander01", 10L, "BASE_COMMANDER");
        when(userRepository.findByUsername("commander01")).thenReturn(Optional.of(baseCommanderUser));
        DashboardResponse mockResponse = new DashboardResponse();
        when(dashboardService.getDashboard(eq(1L), isNull(), isNull())).thenReturn(mockResponse);

        // Attempting to pass baseId=2 (Mangalore)
        ResponseEntity<DashboardResponse> result = dashboardController.getDashboard(2L, null, null);

        assertNotNull(result.getBody());
        // Must override with assigned base (1L)
        verify(dashboardService).getDashboard(eq(1L), isNull(), isNull());
    }

    @Test
    void testLogisticsOfficerIsRestrictedToAssignedBaseWhenRequestingAllBases() {
        setSecurityContext("logistics01", 20L, "LOGISTICS_OFFICER");
        when(userRepository.findByUsername("logistics01")).thenReturn(Optional.of(logisticsOfficerUser));
        DashboardResponse mockResponse = new DashboardResponse();
        when(dashboardService.getDashboard(eq(1L), isNull(), isNull())).thenReturn(mockResponse);

        ResponseEntity<DashboardResponse> result = dashboardController.getDashboard(null, null, null);

        assertNotNull(result.getBody());
        verify(dashboardService).getDashboard(eq(1L), isNull(), isNull());
    }

    @Test
    void testLogisticsOfficerCannotBypassToAnotherBase() {
        setSecurityContext("logistics01", 20L, "LOGISTICS_OFFICER");
        when(userRepository.findByUsername("logistics01")).thenReturn(Optional.of(logisticsOfficerUser));
        DashboardResponse mockResponse = new DashboardResponse();
        when(dashboardService.getDashboard(eq(1L), isNull(), isNull())).thenReturn(mockResponse);

        // Attempting to pass baseId=2 (Mangalore) via direct API/Postman
        ResponseEntity<DashboardResponse> result = dashboardController.getDashboard(2L, null, null);

        assertNotNull(result.getBody());
        // Must override with assigned base (1L)
        verify(dashboardService).getDashboard(eq(1L), isNull(), isNull());
    }

    @Test
    void testLogisticsOfficerWithNoAssignedBaseReturnsSafeEmptyDashboard() {
        User unassignedOfficer = new User("unassignedLog", "unassigned@mat.com", "pass", Role.LOGISTICS_OFFICER, null);
        unassignedOfficer.setId(21L);

        setSecurityContext("unassignedLog", 21L, "LOGISTICS_OFFICER");
        when(userRepository.findByUsername("unassignedLog")).thenReturn(Optional.of(unassignedOfficer));

        ResponseEntity<DashboardResponse> result = dashboardController.getDashboard(null, null, null);

        assertNotNull(result.getBody());
        assertEquals(0, result.getBody().getClosingBalance());
        verifyNoInteractions(dashboardService);
    }

    @Test
    void testBaseCommanderWithNoAssignedBaseReturnsSafeEmptyDashboard() {
        User unassignedCommander = new User("unassignedCmd", "unassigned@mat.com", "pass", Role.BASE_COMMANDER, null);
        unassignedCommander.setId(11L);

        setSecurityContext("unassignedCmd", 11L, "BASE_COMMANDER");
        when(userRepository.findByUsername("unassignedCmd")).thenReturn(Optional.of(unassignedCommander));

        ResponseEntity<DashboardResponse> result = dashboardController.getDashboard(null, null, null);

        assertNotNull(result.getBody());
        assertEquals(0, result.getBody().getClosingBalance());
        verifyNoInteractions(dashboardService);
    }
}
