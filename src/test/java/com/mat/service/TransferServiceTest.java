package com.mat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
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

import com.mat.entity.Base;
import com.mat.entity.Equipment;
import com.mat.entity.Role;
import com.mat.entity.Transfer;
import com.mat.entity.User;
import com.mat.repository.BaseRepository;
import com.mat.repository.EquipmentRepository;
import com.mat.repository.TransferRepository;
import com.mat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private BaseRepository baseRepository;

    @Mock
    private AssetStockService assetStockService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransferService transferService;

    private Base fromBase;
    private Base toBase;
    private Equipment testEquipment;
    private User testUser;

    @BeforeEach
    void setUp() {
        fromBase = new Base("Base Alpha", "Location Alpha");
        fromBase.setId(1L);

        toBase = new Base("Base Bravo", "Location Bravo");
        toBase.setId(2L);

        testEquipment = new Equipment("Tank", "Vehicle", "units");
        testEquipment.setId(1L);

        testUser = new User("transferOfficer", "transfer@mat.com", "password", Role.LOGISTICS_OFFICER, fromBase);
        testUser.setId(15L);
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
    void testSaveTransferAutomaticallySetsAuthenticatedUser() {
        setSecurityContext("transferOfficer", 15L, "LOGISTICS_OFFICER");
        when(userRepository.findByUsername("transferOfficer")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(fromBase));
        when(baseRepository.findById(2L)).thenReturn(Optional.of(toBase));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer t = invocation.getArgument(0);
            t.setId(200L);
            return t;
        });

        Transfer transferInput = new Transfer();
        transferInput.setEquipment(testEquipment);
        transferInput.setFromBase(fromBase);
        transferInput.setToBase(toBase);
        transferInput.setQuantity(5);
        transferInput.setTransferDate(LocalDateTime.now());
        transferInput.setTransferredBy(null);

        Transfer result = transferService.saveTransfer(transferInput);

        assertNotNull(result);
        assertEquals(200L, result.getId());
        assertNotNull(result.getTransferredBy(), "Transfer.transferredBy must not be null");
        assertEquals("transferOfficer", result.getTransferredBy().getUsername());

        // Verify stock decrease at fromBase and increase at toBase
        verify(assetStockService).decreaseStock(testEquipment, fromBase, 5);
        verify(assetStockService).increaseStock(testEquipment, toBase, 5);

        // Verify audit log
        verify(auditLogService).log(
                eq("TRANSFER_CREATED"),
                eq("Transfer"),
                eq(200L),
                eq(testUser),
                any(String.class)
        );
    }

    @Test
    void testSaveTransferThrowsExceptionWhenUnauthenticated() {
        Transfer transferInput = new Transfer(testEquipment, fromBase, toBase, 5, LocalDateTime.now(), null);
        assertThrows(IllegalStateException.class, () -> transferService.saveTransfer(transferInput));
    }

    @Test
    void testSaveTransferSameBaseThrowsException() {
        Transfer transferInput = new Transfer(testEquipment, fromBase, fromBase, 5, LocalDateTime.now(), null);
        assertThrows(IllegalArgumentException.class, () -> transferService.saveTransfer(transferInput));
    }

    @Test
    void testBaseCommanderTransferringFromOwnBaseAllowedIfStockSufficient() {
        User commander = new User("cmdAlpha", "cmdalpha@mat.com", "password", Role.BASE_COMMANDER, fromBase);
        commander.setId(21L);

        setSecurityContext("cmdAlpha", 21L, "BASE_COMMANDER");
        when(userRepository.findByUsername("cmdAlpha")).thenReturn(Optional.of(commander));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(fromBase));
        when(baseRepository.findById(2L)).thenReturn(Optional.of(toBase));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer t = invocation.getArgument(0);
            t.setId(201L);
            return t;
        });

        Transfer transferInput = new Transfer(testEquipment, fromBase, toBase, 5, LocalDateTime.now(), null);
        Transfer result = transferService.saveTransfer(transferInput);

        assertNotNull(result);
        assertEquals(201L, result.getId());
        assertEquals("cmdAlpha", result.getTransferredBy().getUsername());
        verify(assetStockService).decreaseStock(testEquipment, fromBase, 5);
        verify(assetStockService).increaseStock(testEquipment, toBase, 5);
        verify(auditLogService).log(
                eq("TRANSFER_CREATED"),
                eq("Transfer"),
                eq(201L),
                eq(commander),
                any(String.class)
        );
    }

    @Test
    void testBaseCommanderTransferringFromAnotherBaseRejectedWith403() {
        // Commander is assigned to toBase (2L), but attempting to transfer FROM fromBase (1L)
        User commander = new User("cmdBravo", "cmdbravo@mat.com", "password", Role.BASE_COMMANDER, toBase);
        commander.setId(22L);

        setSecurityContext("cmdBravo", 22L, "BASE_COMMANDER");
        when(userRepository.findByUsername("cmdBravo")).thenReturn(Optional.of(commander));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(fromBase));
        when(baseRepository.findById(2L)).thenReturn(Optional.of(toBase));

        Transfer transferInput = new Transfer(testEquipment, fromBase, toBase, 5, LocalDateTime.now(), null);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> transferService.saveTransfer(transferInput));

        assertEquals("Access denied: Base Commander can only transfer assets from their assigned base.", ex.getMessage());
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(assetStockService, never()).decreaseStock(any(), any(), any(int.class));
        verify(assetStockService, never()).increaseStock(any(), any(), any(int.class));
    }

    @Test
    void testBaseCommanderWithNoAssignedBaseRejectedWith403() {
        User commander = new User("cmdUnassigned", "cmdunassigned@mat.com", "password", Role.BASE_COMMANDER, null);
        commander.setId(23L);

        setSecurityContext("cmdUnassigned", 23L, "BASE_COMMANDER");
        when(userRepository.findByUsername("cmdUnassigned")).thenReturn(Optional.of(commander));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(fromBase));
        when(baseRepository.findById(2L)).thenReturn(Optional.of(toBase));

        Transfer transferInput = new Transfer(testEquipment, fromBase, toBase, 5, LocalDateTime.now(), null);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> transferService.saveTransfer(transferInput));

        assertEquals("Access denied: Base Commander can only transfer assets from their assigned base.", ex.getMessage());
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void testAdminTransferringFromAnyBaseAllowedIfStockSufficient() {
        User adminUser = new User("adminMaster", "admin@mat.com", "password", Role.ADMIN, null);
        adminUser.setId(99L);

        setSecurityContext("adminMaster", 99L, "ADMIN");
        when(userRepository.findByUsername("adminMaster")).thenReturn(Optional.of(adminUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(fromBase));
        when(baseRepository.findById(2L)).thenReturn(Optional.of(toBase));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer t = invocation.getArgument(0);
            t.setId(202L);
            return t;
        });

        Transfer transferInput = new Transfer(testEquipment, fromBase, toBase, 10, LocalDateTime.now(), null);
        Transfer result = transferService.saveTransfer(transferInput);

        assertNotNull(result);
        assertEquals(202L, result.getId());
        assertEquals("adminMaster", result.getTransferredBy().getUsername());
        verify(assetStockService).decreaseStock(testEquipment, fromBase, 10);
        verify(assetStockService).increaseStock(testEquipment, toBase, 10);
    }

    @Test
    void testTransferRejectedWhenInsufficientSourceStock() {
        setSecurityContext("transferOfficer", 15L, "LOGISTICS_OFFICER");
        when(userRepository.findByUsername("transferOfficer")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(fromBase));
        when(baseRepository.findById(2L)).thenReturn(Optional.of(toBase));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer t = invocation.getArgument(0);
            t.setId(203L);
            return t;
        });

        doThrow(new IllegalStateException("Insufficient stock for Tank at Base Alpha. Available: 2, Requested: 10."))
                .when(assetStockService).decreaseStock(testEquipment, fromBase, 10);

        Transfer transferInput = new Transfer(testEquipment, fromBase, toBase, 10, LocalDateTime.now(), null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> transferService.saveTransfer(transferInput));

        assertEquals("Insufficient stock for Tank at Base Alpha. Available: 2, Requested: 10.", ex.getMessage());
        verify(assetStockService, never()).increaseStock(any(), any(), any(int.class));
    }
}
