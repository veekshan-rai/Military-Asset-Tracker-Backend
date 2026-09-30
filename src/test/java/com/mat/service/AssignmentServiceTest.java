package com.mat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mat.entity.AssetStock;
import com.mat.entity.Assignment;
import com.mat.entity.Base;
import com.mat.entity.Equipment;
import com.mat.entity.Role;
import com.mat.entity.User;
import com.mat.repository.AssetStockRepository;
import com.mat.repository.AssignmentRepository;
import com.mat.repository.BaseRepository;
import com.mat.repository.EquipmentRepository;
import com.mat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private BaseRepository baseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssetStockRepository assetStockRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private Base testBase;
    private Base otherBase;
    private Equipment testEquipment;
    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testBase = new Base("Delta Base", "Location Delta");
        testBase.setId(1L);

        otherBase = new Base("Echo Base", "Location Echo");
        otherBase.setId(2L);

        testEquipment = new Equipment("Night Vision Goggles", "Optics", "units");
        testEquipment.setId(1L);

        testUser = new User("commanderDelta", "commander@mat.com", "password", Role.BASE_COMMANDER, testBase);
        testUser.setId(30L);

        adminUser = new User("admin01", "admin@mat.com", "password", Role.ADMIN, null);
        adminUser.setId(99L);
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
    void testSaveAssignmentAutomaticallySetsAuthenticatedUser() {
        setSecurityContext("commanderDelta", 30L, "BASE_COMMANDER");
        when(userRepository.findByUsername("commanderDelta")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(testBase));
        AssetStock stock = new AssetStock(testEquipment, testBase, 10);
        when(assetStockRepository.findByEquipment_IdAndBase_Id(1L, 1L)).thenReturn(Optional.of(stock));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment a = invocation.getArgument(0);
            a.setId(400L);
            return a;
        });

        Assignment assignmentInput = new Assignment();
        assignmentInput.setEquipment(testEquipment);
        assignmentInput.setBase(testBase);
        assignmentInput.setPersonnelName("Sgt. John Doe");
        assignmentInput.setQuantity(2);
        assignmentInput.setAssignmentDate(LocalDateTime.now());
        assignmentInput.setAssignedBy(null);

        Assignment result = assignmentService.saveAssignment(assignmentInput);

        assertNotNull(result);
        assertEquals(400L, result.getId());
        assertNotNull(result.getAssignedBy(), "Assignment.assignedBy must not be null");
        assertEquals("commanderDelta", result.getAssignedBy().getUsername());
        assertEquals(30L, result.getAssignedBy().getId());
    }

    @Test
    void testSaveAssignmentThrowsExceptionWhenUnauthenticated() {
        Assignment assignmentInput = new Assignment(testEquipment, testBase, "Sgt. John Doe", 2, LocalDateTime.now(), null);
        assertThrows(IllegalStateException.class, () -> assignmentService.saveAssignment(assignmentInput));
    }

    @Test
    void testBaseCommanderAssigningToOwnBaseAllowedIfStockIsSufficient() {
        setSecurityContext("commanderDelta", 30L, "BASE_COMMANDER");
        when(userRepository.findByUsername("commanderDelta")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(testBase));
        AssetStock stock = new AssetStock(testEquipment, testBase, 15);
        when(assetStockRepository.findByEquipment_IdAndBase_Id(1L, 1L)).thenReturn(Optional.of(stock));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment a = invocation.getArgument(0);
            a.setId(501L);
            return a;
        });

        Assignment assignmentInput = new Assignment(testEquipment, testBase, "Cpl. Wayne", 3, LocalDateTime.now(), null);

        Assignment result = assignmentService.saveAssignment(assignmentInput);

        assertNotNull(result);
        assertEquals(501L, result.getId());
        assertEquals("Cpl. Wayne", result.getPersonnelName());
        assertEquals(3, result.getQuantity());
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void testBaseCommanderAssigningToAnotherBaseRejectedWith403() {
        setSecurityContext("commanderDelta", 30L, "BASE_COMMANDER");
        when(userRepository.findByUsername("commanderDelta")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(2L)).thenReturn(Optional.of(otherBase));

        // Commander is assigned to Delta Base (1L), but requested assignment is at Echo Base (2L)
        Assignment assignmentInput = new Assignment(testEquipment, otherBase, "Sgt. John Doe", 2, LocalDateTime.now(), null);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> assignmentService.saveAssignment(assignmentInput));

        assertEquals("Access denied: Base Commander is only authorized to assign equipment at their assigned base.", ex.getMessage());
        verify(assignmentRepository, never()).save(any(Assignment.class));
        verify(assetStockRepository, never()).findByEquipment_IdAndBase_Id(any(), any());
    }

    @Test
    void testBaseCommanderWithNoAssignedBaseRejectedWith403() {
        User unassignedCommander = new User("unassignedCmd", "unassigned@mat.com", "pass", Role.BASE_COMMANDER, null);
        unassignedCommander.setId(31L);

        setSecurityContext("unassignedCmd", 31L, "BASE_COMMANDER");
        when(userRepository.findByUsername("unassignedCmd")).thenReturn(Optional.of(unassignedCommander));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(testBase));

        Assignment assignmentInput = new Assignment(testEquipment, testBase, "Sgt. John Doe", 2, LocalDateTime.now(), null);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> assignmentService.saveAssignment(assignmentInput));

        assertEquals("Access denied: Base Commander is only authorized to assign equipment at their assigned base.", ex.getMessage());
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testAdminAssigningToAnotherBaseAllowedIfStockIsSufficient() {
        setSecurityContext("admin01", 99L, "ADMIN");
        when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(2L)).thenReturn(Optional.of(otherBase));

        // Stock at otherBase (Echo Base, ID 2)
        AssetStock stockAtOtherBase = new AssetStock(testEquipment, otherBase, 20);
        when(assetStockRepository.findByEquipment_IdAndBase_Id(1L, 2L)).thenReturn(Optional.of(stockAtOtherBase));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment a = invocation.getArgument(0);
            a.setId(777L);
            return a;
        });

        Assignment assignmentInput = new Assignment(testEquipment, otherBase, "Lt. Miller", 5, LocalDateTime.now(), null);

        Assignment result = assignmentService.saveAssignment(assignmentInput);

        assertNotNull(result);
        assertEquals(777L, result.getId());
        assertEquals("Echo Base", result.getBase().getName());
        assertEquals("admin01", result.getAssignedBy().getUsername());
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void testSaveAssignmentWithZeroStockRejectedWhenNoStockRecordExists() {
        setSecurityContext("commanderDelta", 30L, "BASE_COMMANDER");
        when(userRepository.findByUsername("commanderDelta")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(testBase));
        when(assetStockRepository.findByEquipment_IdAndBase_Id(1L, 1L)).thenReturn(Optional.empty());

        Assignment assignmentInput = new Assignment(testEquipment, testBase, "Sgt. John Doe", 2, LocalDateTime.now(), null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> assignmentService.saveAssignment(assignmentInput));
        assertEquals("Insufficient stock: No Night Vision Goggles available at Delta Base.", ex.getMessage());
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testSaveAssignmentWithZeroStockRejectedWhenStockQuantityIsZero() {
        setSecurityContext("commanderDelta", 30L, "BASE_COMMANDER");
        when(userRepository.findByUsername("commanderDelta")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(testBase));
        AssetStock zeroStock = new AssetStock(testEquipment, testBase, 0);
        when(assetStockRepository.findByEquipment_IdAndBase_Id(1L, 1L)).thenReturn(Optional.of(zeroStock));

        Assignment assignmentInput = new Assignment(testEquipment, testBase, "Sgt. John Doe", 2, LocalDateTime.now(), null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> assignmentService.saveAssignment(assignmentInput));
        assertEquals("Insufficient stock: Only 0 Night Vision Goggles available at Delta Base.", ex.getMessage());
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testSaveAssignmentWithInsufficientStockRejected() {
        setSecurityContext("commanderDelta", 30L, "BASE_COMMANDER");
        when(userRepository.findByUsername("commanderDelta")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(testBase));
        AssetStock lowStock = new AssetStock(testEquipment, testBase, 3);
        when(assetStockRepository.findByEquipment_IdAndBase_Id(1L, 1L)).thenReturn(Optional.of(lowStock));

        Assignment assignmentInput = new Assignment(testEquipment, testBase, "Sgt. John Doe", 5, LocalDateTime.now(), null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> assignmentService.saveAssignment(assignmentInput));
        assertEquals("Insufficient stock: Only 3 Night Vision Goggles available at Delta Base.", ex.getMessage());
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testSaveAssignmentWithSufficientStockAllowed() {
        setSecurityContext("commanderDelta", 30L, "BASE_COMMANDER");
        when(userRepository.findByUsername("commanderDelta")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(testBase));
        AssetStock sufficientStock = new AssetStock(testEquipment, testBase, 10);
        when(assetStockRepository.findByEquipment_IdAndBase_Id(1L, 1L)).thenReturn(Optional.of(sufficientStock));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment a = invocation.getArgument(0);
            a.setId(500L);
            return a;
        });

        Assignment assignmentInput = new Assignment(testEquipment, testBase, "Sgt. John Doe", 4, LocalDateTime.now(), null);

        Assignment result = assignmentService.saveAssignment(assignmentInput);
        assertNotNull(result);
        assertEquals(500L, result.getId());
        assertEquals("Sgt. John Doe", result.getPersonnelName());
        assertEquals(4, result.getQuantity());
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void testSuccessfulAssignmentDoesNotReduceStock() {
        setSecurityContext("commanderDelta", 30L, "BASE_COMMANDER");
        when(userRepository.findByUsername("commanderDelta")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(testBase));
        AssetStock stock = new AssetStock(testEquipment, testBase, 10);
        when(assetStockRepository.findByEquipment_IdAndBase_Id(1L, 1L)).thenReturn(Optional.of(stock));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Assignment assignmentInput = new Assignment(testEquipment, testBase, "Sgt. John Doe", 4, LocalDateTime.now(), null);

        assignmentService.saveAssignment(assignmentInput);

        // Verify stock quantity remains unchanged (10)
        assertEquals(10, stock.getQuantity());
        // Verify AssetStockRepository is never saved/modified
        verify(assetStockRepository, never()).save(any(AssetStock.class));
    }

    @Test
    void testGetAllAssignments() {
        when(assignmentRepository.findAll()).thenReturn(Collections.emptyList());
        List<Assignment> result = assignmentService.getAllAssignments();
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(assignmentRepository).findAll();
    }
}
