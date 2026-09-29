package com.mat.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mat.dto.DashboardResponse;
import com.mat.entity.AssetStock;
import com.mat.entity.Assignment;
import com.mat.entity.Base;
import com.mat.entity.Equipment;
import com.mat.entity.Expenditure;
import com.mat.entity.Purchase;
import com.mat.repository.AssetStockRepository;
import com.mat.repository.AssignmentRepository;
import com.mat.repository.BaseRepository;
import com.mat.repository.ExpenditureRepository;
import com.mat.repository.PurchaseRepository;
import com.mat.repository.TransferRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private ExpenditureRepository expenditureRepository;
    @Mock
    private AssetStockRepository assetStockRepository;
    @Mock
    private BaseRepository baseRepository;

    private DashboardService dashboardService;

    private Base baseAlpha;
    private Equipment rifle;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dashboardService = new DashboardService(
                purchaseRepository,
                transferRepository,
                assignmentRepository,
                expenditureRepository,
                assetStockRepository,
                baseRepository
        );

        baseAlpha = new Base("Base Alpha", "Sector 1");
        baseAlpha.setId(1L);

        rifle = new Equipment("M4 Carbine", "WEAPON", "pcs");
        rifle.setId(10L);
    }

    @Test
    void testOpeningBalanceWithoutDateFilter_AccountsForExpenditure() {
        // Given 111 purchases, 102 expenditures, 9 remaining stock
        Purchase p1 = new Purchase(rifle, baseAlpha, 111, LocalDateTime.now().minusDays(10), null);
        Expenditure e1 = new Expenditure(rifle, baseAlpha, 102, LocalDateTime.now().minusDays(5), "Training", null);
        AssetStock stock = new AssetStock(rifle, baseAlpha, 9);

        when(purchaseRepository.findAll()).thenReturn(List.of(p1));
        when(expenditureRepository.findAll()).thenReturn(List.of(e1));
        when(transferRepository.findAll()).thenReturn(List.of());
        when(assignmentRepository.findAll()).thenReturn(List.of());
        when(assetStockRepository.findAll()).thenReturn(List.of(stock));

        // When requesting dashboard without date filter
        DashboardResponse response = dashboardService.getDashboard(null, null, null);

        // Then opening balance should be 0 (9 - 111 + 102 = 0) and NOT negative -102
        assertEquals(9, response.getClosingBalance());
        assertEquals(111, response.getPurchases());
        assertEquals(111, response.getNetMovement());
        assertEquals(102, response.getExpended());
        assertEquals(0, response.getOpeningBalance());
    }

    @Test
    void testOpeningBalanceWithDateFilter_CalculatesPriorMovementsAndExpenditures() {
        LocalDate selectedDate = LocalDate.of(2026, 9, 30);
        LocalDateTime priorDate = LocalDateTime.of(2026, 9, 28, 10, 0);
        LocalDateTime currentDate = LocalDateTime.of(2026, 9, 30, 14, 0);

        // Prior transactions (before 2026-09-30)
        Purchase pPrior = new Purchase(rifle, baseAlpha, 100, priorDate, null);
        Expenditure ePrior = new Expenditure(rifle, baseAlpha, 15, priorDate, "Exercise", null);

        // Selected date transactions (on 2026-09-30)
        Purchase pCurrent = new Purchase(rifle, baseAlpha, 20, currentDate, null);

        AssetStock stock = new AssetStock(rifle, baseAlpha, 105);

        when(purchaseRepository.findByPurchaseDateBetween(any(), any())).thenReturn(List.of(pCurrent));
        when(expenditureRepository.findAll()).thenReturn(List.of(ePrior));
        when(purchaseRepository.findAll()).thenReturn(List.of(pPrior, pCurrent));
        when(transferRepository.findAll()).thenReturn(List.of());
        when(assignmentRepository.findAll()).thenReturn(List.of());
        when(assetStockRepository.findAll()).thenReturn(List.of(stock));

        // When requesting dashboard for selectedDate
        DashboardResponse response = dashboardService.getDashboard(null, null, selectedDate);

        // Opening balance before 2026-09-30 should be 100 (purchase) - 15 (expenditure) = 85
        assertEquals(85, response.getOpeningBalance());
        assertEquals(20, response.getPurchases());
        assertEquals(20, response.getNetMovement());
        assertEquals(105, response.getClosingBalance());
    }

    @Test
    void testAssignmentsDoNotReduceOpeningBalanceOrStock() {
        Purchase p = new Purchase(rifle, baseAlpha, 50, LocalDateTime.now(), null);
        Assignment a = new Assignment(rifle, baseAlpha, "Sgt. Smith", 30, LocalDateTime.now(), null);
        AssetStock stock = new AssetStock(rifle, baseAlpha, 50);

        when(purchaseRepository.findAll()).thenReturn(List.of(p));
        when(assignmentRepository.findAll()).thenReturn(List.of(a));
        when(expenditureRepository.findAll()).thenReturn(List.of());
        when(transferRepository.findAll()).thenReturn(List.of());
        when(assetStockRepository.findAll()).thenReturn(List.of(stock));

        DashboardResponse response = dashboardService.getDashboard(null, null, null);

        assertEquals(30, response.getAssigned());
        assertEquals(0, response.getExpended());
        assertEquals(50, response.getClosingBalance());
        assertEquals(0, response.getOpeningBalance());
    }
}
