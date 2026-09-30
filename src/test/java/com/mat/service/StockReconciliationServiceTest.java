package com.mat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mat.entity.AssetStock;
import com.mat.entity.Base;
import com.mat.entity.Equipment;
import com.mat.entity.Expenditure;
import com.mat.entity.Purchase;
import com.mat.entity.Transfer;
import com.mat.repository.AssetStockRepository;
import com.mat.repository.BaseRepository;
import com.mat.repository.EquipmentRepository;
import com.mat.repository.ExpenditureRepository;
import com.mat.repository.PurchaseRepository;
import com.mat.repository.TransferRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class StockReconciliationServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private ExpenditureRepository expenditureRepository;
    @Mock
    private AssetStockRepository assetStockRepository;
    @Mock
    private BaseRepository baseRepository;
    @Mock
    private EquipmentRepository equipmentRepository;

    private StockReconciliationService reconciliationService;

    private Base baseAlpha;
    private Base baseBeta;
    private Equipment rifle;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reconciliationService = new StockReconciliationService(
                purchaseRepository,
                transferRepository,
                expenditureRepository,
                assetStockRepository,
                baseRepository,
                equipmentRepository
        );

        baseAlpha = new Base("Base Alpha", "Sector 1");
        baseAlpha.setId(1L);

        baseBeta = new Base("Base Beta", "Sector 2");
        baseBeta.setId(2L);

        rifle = new Equipment("M4 Carbine", "WEAPON", "pcs");
        rifle.setId(10L);

        when(baseRepository.findAll()).thenReturn(List.of(baseAlpha, baseBeta));
        when(equipmentRepository.findAll()).thenReturn(List.of(rifle));
    }

    @Test
    void testStockReconciliationPerEquipmentAndBase() {
        // Purchases = 111 at Base Alpha
        Purchase p1 = new Purchase(rifle, baseAlpha, 111, LocalDateTime.now().minusDays(10), null);
        // Transfer 25 from Alpha to Beta
        Transfer t1 = new Transfer(rifle, baseAlpha, baseBeta, 25, LocalDateTime.now().minusDays(5), null);
        // Expended 4 at Base Alpha
        Expenditure e1 = new Expenditure(rifle, baseAlpha, 4, LocalDateTime.now().minusDays(2), "Training", null);

        // Current AssetStock in DB: Base Alpha = 9, Base Beta = 25
        AssetStock stockAlpha = new AssetStock(rifle, baseAlpha, 9);
        AssetStock stockBeta = new AssetStock(rifle, baseBeta, 25);

        when(purchaseRepository.findAll()).thenReturn(List.of(p1));
        when(transferRepository.findAll()).thenReturn(List.of(t1));
        when(expenditureRepository.findAll()).thenReturn(List.of(e1));
        when(assetStockRepository.findAll()).thenReturn(List.of(stockAlpha, stockBeta));

        List<StockReconciliationService.ReconciliationItem> report = reconciliationService.generateReconciliationReport();

        assertEquals(2, report.size());

        StockReconciliationService.ReconciliationItem itemAlpha = report.stream()
                .filter(i -> i.getBaseId().equals(1L))
                .findFirst().orElseThrow();

        // Base Alpha Expected = 111 (purchases) - 25 (transfer out) - 4 (expenditures) = 82
        // Base Alpha Current = 9
        // Diff = 82 - 9 = +73
        assertEquals(9, itemAlpha.getCurrentStock());
        assertEquals(82, itemAlpha.getExpectedStock());
        assertEquals(73, itemAlpha.getDifference());

        StockReconciliationService.ReconciliationItem itemBeta = report.stream()
                .filter(i -> i.getBaseId().equals(2L))
                .findFirst().orElseThrow();

        // Base Beta Expected = 0 + 25 (transfer in) = 25
        // Base Beta Current = 25
        // Diff = 0
        assertEquals(25, itemBeta.getCurrentStock());
        assertEquals(25, itemBeta.getExpectedStock());
        assertEquals(0, itemBeta.getDifference());

        String sqlScript = reconciliationService.generateSqlMigrationScript();
        assertTrue(sqlScript.contains("INSERT INTO asset_stock (equipment_id, base_id, quantity) VALUES (10, 1, 82)"));
    }
}
