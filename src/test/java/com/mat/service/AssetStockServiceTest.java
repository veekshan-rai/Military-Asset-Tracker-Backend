package com.mat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mat.entity.AssetStock;
import com.mat.entity.Base;
import com.mat.entity.Equipment;
import com.mat.entity.Role;
import com.mat.entity.User;
import com.mat.repository.AssetStockRepository;
import com.mat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AssetStockServiceTest {

    @Mock
    private AssetStockRepository assetStockRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssetStockService assetStockService;

    private Base base1;
    private Base base2;
    private Equipment eq1;
    private AssetStock stock1;
    private AssetStock stock2;

    @BeforeEach
    void setUp() {
        base1 = new Base("Bangalore", "Bangalore");
        base1.setId(1L);

        base2 = new Base("Mangalore", "Mangalore");
        base2.setId(2L);

        eq1 = new Equipment("AK-47", "Weapon", "pieces");
        eq1.setId(1L);

        stock1 = new AssetStock(eq1, base1, 80);
        stock1.setId(101L);

        stock2 = new AssetStock(eq1, base2, 20);
        stock2.setId(102L);
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
    void testAdminCanSeeAllStock() {
        setSecurityContext("admin01", 1L, "ADMIN");
        when(assetStockRepository.findAll()).thenReturn(List.of(stock1, stock2));

        List<AssetStock> result = assetStockService.getStock(null);

        assertEquals(2, result.size());
        verify(assetStockRepository).findAll();
    }

    @Test
    void testAdminCanFilterByBase() {
        setSecurityContext("admin01", 1L, "ADMIN");
        when(assetStockRepository.findByBase_Id(1L)).thenReturn(List.of(stock1));

        List<AssetStock> result = assetStockService.getStock(1L);

        assertEquals(1, result.size());
        assertEquals("Bangalore", result.get(0).getBase().getName());
        verify(assetStockRepository).findByBase_Id(1L);
    }

    @Test
    void testBaseCommanderSeesOnlyAssignedBaseStock() {
        setSecurityContext("commander01", 2L, "BASE_COMMANDER");

        User commander = new User("commander01", "cmd@mat.com", "pass", Role.BASE_COMMANDER, base1);
        commander.setId(2L);
        when(userRepository.findByUsername("commander01")).thenReturn(Optional.of(commander));
        when(assetStockRepository.findByBase_Id(1L)).thenReturn(List.of(stock1));

        // When requesting with no baseId
        List<AssetStock> result = assetStockService.getStock(null);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getBase().getId());
        assertEquals("Bangalore", result.get(0).getBase().getName());
        verify(assetStockRepository).findByBase_Id(1L);
    }

    @Test
    void testBaseCommanderCannotSeeOtherBaseStockEvenIfRequested() {
        setSecurityContext("commander01", 2L, "BASE_COMMANDER");

        User commander = new User("commander01", "cmd@mat.com", "pass", Role.BASE_COMMANDER, base1);
        commander.setId(2L);
        when(userRepository.findByUsername("commander01")).thenReturn(Optional.of(commander));
        when(assetStockRepository.findByBase_Id(1L)).thenReturn(List.of(stock1));

        // Even if commander requests baseId = 2 (Mangalore), they only get their assigned base (Base 1)
        List<AssetStock> result = assetStockService.getStock(2L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getBase().getId());
        verify(assetStockRepository).findByBase_Id(1L);
    }

    @Test
    void testBaseCommanderWithNoAssignedBaseGetsEmptyList() {
        setSecurityContext("commanderUnassigned", 3L, "BASE_COMMANDER");

        User commander = new User("commanderUnassigned", "cmd2@mat.com", "pass", Role.BASE_COMMANDER, null);
        commander.setId(3L);
        when(userRepository.findByUsername("commanderUnassigned")).thenReturn(Optional.of(commander));

        List<AssetStock> result = assetStockService.getStock(null);

        assertEquals(0, result.size());
    }

    @Test
    void testLogisticsOfficerSeesAllStock() {
        setSecurityContext("logistics01", 4L, "LOGISTICS_OFFICER");
        when(assetStockRepository.findAll()).thenReturn(List.of(stock1, stock2));

        List<AssetStock> result = assetStockService.getStock(null);

        assertEquals(2, result.size());
        verify(assetStockRepository).findAll();
    }

    @Test
    void testLogisticsOfficerCanFilterByBase() {
        setSecurityContext("logistics01", 4L, "LOGISTICS_OFFICER");
        when(assetStockRepository.findByBase_Id(2L)).thenReturn(List.of(stock2));

        List<AssetStock> result = assetStockService.getStock(2L);

        assertEquals(1, result.size());
        assertEquals("Mangalore", result.get(0).getBase().getName());
        verify(assetStockRepository).findByBase_Id(2L);
    }
}
