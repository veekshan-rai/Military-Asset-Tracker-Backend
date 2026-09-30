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
import com.mat.entity.Purchase;
import com.mat.entity.Role;
import com.mat.entity.User;
import com.mat.repository.BaseRepository;
import com.mat.repository.EquipmentRepository;
import com.mat.repository.PurchaseRepository;
import com.mat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

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
    private PurchaseService purchaseService;

    private Base testBase;
    private Equipment testEquipment;
    private User testUser;

    @BeforeEach
    void setUp() {
        testBase = new Base("Test Base", "Location A");
        testBase.setId(1L);

        testEquipment = new Equipment("Rifle", "Weapon", "pieces");
        testEquipment.setId(1L);

        testUser = new User("officer1", "officer1@mat.com", "password", Role.LOGISTICS_OFFICER, testBase);
        testUser.setId(10L);
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
    void testSavePurchaseAutomaticallySetsAuthenticatedUser() {
        setSecurityContext("officer1", 10L, "LOGISTICS_OFFICER");
        when(userRepository.findByUsername("officer1")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(testBase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });

        // Purchase payload without recordedBy (as sent by React frontend)
        Purchase purchaseInput = new Purchase();
        purchaseInput.setEquipment(testEquipment);
        purchaseInput.setBase(testBase);
        purchaseInput.setQuantity(50);
        purchaseInput.setPurchaseDate(LocalDateTime.now());
        purchaseInput.setRecordedBy(null);

        Purchase result = purchaseService.savePurchase(purchaseInput);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertNotNull(result.getRecordedBy(), "Purchase.recordedBy must not be null");
        assertEquals("officer1", result.getRecordedBy().getUsername());
        assertEquals(10L, result.getRecordedBy().getId());

        // Verify stock increase
        verify(assetStockService).increaseStock(testEquipment, testBase, 50);

        // Verify audit log
        verify(auditLogService).log(
                eq("PURCHASE_CREATED"),
                eq("Purchase"),
                eq(100L),
                eq(testUser),
                any(String.class)
        );
    }

    @Test
    void testSavePurchaseOverridesFrontendProvidedUser() {
        setSecurityContext("officer1", 10L, "LOGISTICS_OFFICER");
        when(userRepository.findByUsername("officer1")).thenReturn(Optional.of(testUser));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(baseRepository.findById(1L)).thenReturn(Optional.of(testBase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase p = invocation.getArgument(0);
            p.setId(101L);
            return p;
        });

        // Untrusted user provided by client
        User untrustedUser = new User("hacker", "hack@evil.com", "pass", Role.ADMIN, null);
        untrustedUser.setId(999L);

        Purchase purchaseInput = new Purchase(testEquipment, testBase, 25, LocalDateTime.now(), untrustedUser);

        Purchase result = purchaseService.savePurchase(purchaseInput);

        assertNotNull(result);
        assertEquals(testUser, result.getRecordedBy(), "Should overwrite client-sent user with authenticated user");
        assertEquals("officer1", result.getRecordedBy().getUsername());
    }

    @Test
    void testSavePurchaseThrowsExceptionWhenUnauthenticated() {
        // No security context set
        Purchase purchaseInput = new Purchase();
        purchaseInput.setEquipment(testEquipment);
        purchaseInput.setBase(testBase);
        purchaseInput.setQuantity(10);
        purchaseInput.setPurchaseDate(LocalDateTime.now());

        assertThrows(IllegalStateException.class, () -> purchaseService.savePurchase(purchaseInput));
    }

    @Test
    void testSavePurchaseValidationErrors() {
        setSecurityContext("officer1", 10L, "LOGISTICS_OFFICER");

        Purchase noEq = new Purchase(null, testBase, 10, LocalDateTime.now(), null);
        assertThrows(IllegalArgumentException.class, () -> purchaseService.savePurchase(noEq));

        Purchase noBase = new Purchase(testEquipment, null, 10, LocalDateTime.now(), null);
        assertThrows(IllegalArgumentException.class, () -> purchaseService.savePurchase(noBase));

        Purchase invalidQty = new Purchase(testEquipment, testBase, 0, LocalDateTime.now(), null);
        assertThrows(IllegalArgumentException.class, () -> purchaseService.savePurchase(invalidQty));

        Purchase noDate = new Purchase(testEquipment, testBase, 10, null, null);
        assertThrows(IllegalArgumentException.class, () -> purchaseService.savePurchase(noDate));
    }
}
