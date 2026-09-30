package com.mat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mat.entity.Base;
import com.mat.entity.Equipment;
import com.mat.entity.Expenditure;
import com.mat.entity.Role;
import com.mat.entity.User;
import com.mat.repository.BaseRepository;
import com.mat.repository.EquipmentRepository;
import com.mat.repository.ExpenditureRepository;
import com.mat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ExpenditureServiceTest {

    @Mock
    private ExpenditureRepository expenditureRepository;

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
    private ExpenditureService expenditureService;

    private Base testBase;
    private Equipment testEquipment;
    private User testUser;

    @BeforeEach
    void setUp() {
        testBase = new Base("Camp Charlie", "Location Charlie");
        testBase.setId(1L);

        testEquipment = new Equipment("Ammunition 5.56mm", "Ammunition", "rounds");
        testEquipment.setId(1L);

        testUser = new User("officerExpenditure", "officer@mat.com", "password", Role.LOGISTICS_OFFICER, testBase);
        testUser.setId(20L);
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
    void testSaveExpenditureAutomaticallySetsAuthenticatedUser() {
        setSecurityContext("officerExpenditure", 20L, "LOGISTICS_OFFICER");
        when(userRepository.findByUsername("officerExpenditure")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(testBase));
        when(expenditureRepository.save(any(Expenditure.class))).thenAnswer(invocation -> {
            Expenditure e = invocation.getArgument(0);
            e.setId(300L);
            return e;
        });

        Expenditure expenditureInput = new Expenditure();
        expenditureInput.setEquipment(testEquipment);
        expenditureInput.setBase(testBase);
        expenditureInput.setQuantity(200);
        expenditureInput.setExpenditureDate(LocalDateTime.now());
        expenditureInput.setReason("Field training exercise");
        expenditureInput.setRecordedBy(null);

        Expenditure result = expenditureService.saveExpenditure(expenditureInput);

        assertNotNull(result);
        assertEquals(300L, result.getId());
        assertNotNull(result.getRecordedBy(), "Expenditure.recordedBy must not be null");
        assertEquals("officerExpenditure", result.getRecordedBy().getUsername());

        // Verify stock decrease
        verify(assetStockService).decreaseStock(testEquipment, testBase, 200);

        // Verify audit log
        verify(auditLogService).log(
                eq("EXPENDITURE_CREATED"),
                eq("Expenditure"),
                eq(300L),
                eq(testUser),
                any(String.class)
        );
    }

    @Test
    void testSaveExpenditureThrowsExceptionWhenUnauthenticated() {
        Expenditure expenditureInput = new Expenditure(testEquipment, testBase, 50, LocalDateTime.now(), "Training", null);
        assertThrows(IllegalStateException.class, () -> expenditureService.saveExpenditure(expenditureInput));
    }
}
