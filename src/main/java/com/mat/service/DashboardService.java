package com.mat.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mat.dto.DashboardResponse;
import com.mat.entity.Assignment;
import com.mat.entity.Base;
import com.mat.entity.Expenditure;
import com.mat.entity.Purchase;
import com.mat.entity.Transfer;
import com.mat.repository.AssignmentRepository;
import com.mat.repository.AssetStockRepository;
import com.mat.repository.BaseRepository;
import com.mat.repository.ExpenditureRepository;
import com.mat.repository.PurchaseRepository;
import com.mat.repository.TransferRepository;

/**
 * DashboardService
 *
 * Calculates dashboard metrics using transaction records.
 *
 * Business formulas:
 *   Net Movement    = Purchases + Transfer In - Transfer Out
 *   Closing Balance = Opening Balance + Net Movement
 *
 * Opening Balance is the current AssetStock minus today's net movement
 * (or calculated from the start of the filtered date).
 */
@Service
public class DashboardService {

    private final PurchaseRepository purchaseRepository;
    private final TransferRepository transferRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExpenditureRepository expenditureRepository;
    private final AssetStockRepository assetStockRepository;
    private final BaseRepository baseRepository;

    public DashboardService(PurchaseRepository purchaseRepository,
                            TransferRepository transferRepository,
                            AssignmentRepository assignmentRepository,
                            ExpenditureRepository expenditureRepository,
                            AssetStockRepository assetStockRepository,
                            BaseRepository baseRepository) {
        this.purchaseRepository = purchaseRepository;
        this.transferRepository = transferRepository;
        this.assignmentRepository = assignmentRepository;
        this.expenditureRepository = expenditureRepository;
        this.assetStockRepository = assetStockRepository;
        this.baseRepository = baseRepository;
    }

    /**
     * Get dashboard data with optional filters.
     *
     * @param baseId        optional base ID filter (null = all bases)
     * @param equipmentType optional equipment type filter (applied after aggregation)
     * @param date          optional date filter (only count transactions on that date)
     * @return DashboardResponse with all metrics
     */
    public DashboardResponse getDashboard(Long baseId, String equipmentType, LocalDate date) {

        DashboardResponse response = new DashboardResponse();

        Base base = null;
        if (baseId != null) {
            base = baseRepository.findById(baseId)
                    .orElseThrow(() -> new IllegalArgumentException("Base not found with ID: " + baseId));
        }

        // Determine date range for filtering
        final LocalDateTime startOfDay = date != null ? date.atStartOfDay() : null;
        final LocalDateTime endOfDay = date != null ? date.atTime(23, 59, 59) : null;

        // ── Calculate Purchases ──
        List<Purchase> purchases;
        if (base != null && date != null) {
            purchases = purchaseRepository.findByBaseAndPurchaseDateBetween(base, startOfDay, endOfDay);
        } else if (base != null) {
            purchases = purchaseRepository.findByBase(base);
        } else if (date != null) {
            purchases = purchaseRepository.findByPurchaseDateBetween(startOfDay, endOfDay);
        } else {
            purchases = purchaseRepository.findAll();
        }

        // Apply equipment type filter
        if (equipmentType != null && !equipmentType.isEmpty()) {
            purchases = purchases.stream()
                    .filter(p -> equipmentType.equals(p.getEquipment().getEquipmentType()))
                    .toList();
        }

        int totalPurchases = purchases.stream()
                .mapToInt(Purchase::getQuantity)
                .sum();

        // ── Calculate Transfers ──
        int totalTransferIn = 0;
        int totalTransferOut = 0;

        if (base != null && date != null) {
            List<Transfer> transfersOut = transferRepository
                    .findByFromBaseAndTransferDateBetween(base, startOfDay, endOfDay);
            List<Transfer> transfersIn = transferRepository
                    .findByToBaseAndTransferDateBetween(base, startOfDay, endOfDay);

            if (equipmentType != null && !equipmentType.isEmpty()) {
                transfersOut = transfersOut.stream()
                        .filter(t -> equipmentType.equals(t.getEquipment().getEquipmentType()))
                        .toList();
                transfersIn = transfersIn.stream()
                        .filter(t -> equipmentType.equals(t.getEquipment().getEquipmentType()))
                        .toList();
            }

            totalTransferOut = transfersOut.stream().mapToInt(Transfer::getQuantity).sum();
            totalTransferIn = transfersIn.stream().mapToInt(Transfer::getQuantity).sum();

        } else if (base != null) {
            List<Transfer> transfersOut = transferRepository.findByFromBase(base);
            List<Transfer> transfersIn = transferRepository.findByToBase(base);

            if (equipmentType != null && !equipmentType.isEmpty()) {
                transfersOut = transfersOut.stream()
                        .filter(t -> equipmentType.equals(t.getEquipment().getEquipmentType()))
                        .toList();
                transfersIn = transfersIn.stream()
                        .filter(t -> equipmentType.equals(t.getEquipment().getEquipmentType()))
                        .toList();
            }

            totalTransferOut = transfersOut.stream().mapToInt(Transfer::getQuantity).sum();
            totalTransferIn = transfersIn.stream().mapToInt(Transfer::getQuantity).sum();

        } else {
            // When no base filter, transfer in/out across system cancel out
            // But we still report totals for visibility
            List<Transfer> allTransfers;
            if (date != null) {
                allTransfers = transferRepository.findAll().stream()
                        .filter(t -> !t.getTransferDate().isBefore(startOfDay) && !t.getTransferDate().isAfter(endOfDay))
                        .toList();
            } else {
                allTransfers = transferRepository.findAll();
            }

            if (equipmentType != null && !equipmentType.isEmpty()) {
                allTransfers = allTransfers.stream()
                        .filter(t -> equipmentType.equals(t.getEquipment().getEquipmentType()))
                        .toList();
            }

            totalTransferIn = allTransfers.stream().mapToInt(Transfer::getQuantity).sum();
            totalTransferOut = allTransfers.stream().mapToInt(Transfer::getQuantity).sum();
        }

        // ── Calculate Expenditures ──
        List<Expenditure> expenditures;
        if (base != null && date != null) {
            expenditures = expenditureRepository
                    .findByBaseAndExpenditureDateBetween(base, startOfDay, endOfDay);
        } else if (base != null) {
            expenditures = expenditureRepository.findByBase(base);
        } else if (date != null) {
            expenditures = expenditureRepository.findAll().stream()
                    .filter(e -> !e.getExpenditureDate().isBefore(startOfDay) && !e.getExpenditureDate().isAfter(endOfDay))
                    .toList();
        } else {
            expenditures = expenditureRepository.findAll();
        }

        if (equipmentType != null && !equipmentType.isEmpty()) {
            expenditures = expenditures.stream()
                    .filter(e -> equipmentType.equals(e.getEquipment().getEquipmentType()))
                    .toList();
        }

        int totalExpended = expenditures.stream()
                .mapToInt(Expenditure::getQuantity)
                .sum();

        // ── Calculate Assignments ──
        List<Assignment> assignments;
        if (base != null && date != null) {
            assignments = assignmentRepository
                    .findByBaseAndAssignmentDateBetween(base, startOfDay, endOfDay);
        } else if (base != null) {
            assignments = assignmentRepository.findByBase(base);
        } else if (date != null) {
            assignments = assignmentRepository.findAll().stream()
                    .filter(a -> !a.getAssignmentDate().isBefore(startOfDay) && !a.getAssignmentDate().isAfter(endOfDay))
                    .toList();
        } else {
            assignments = assignmentRepository.findAll();
        }

        if (equipmentType != null && !equipmentType.isEmpty()) {
            assignments = assignments.stream()
                    .filter(a -> equipmentType.equals(a.getEquipment().getEquipmentType()))
                    .toList();
        }

        int totalAssigned = assignments.stream()
                .mapToInt(Assignment::getQuantity)
                .sum();

        // ── Calculate Closing Balance (current stock) ──
        int closingBalance;
        if (baseId != null) {
            closingBalance = assetStockRepository.findByBase_Id(baseId).stream()
                    .filter(s -> equipmentType == null || equipmentType.isEmpty()
                            || equipmentType.equals(s.getEquipment().getEquipmentType()))
                    .mapToInt(s -> s.getQuantity())
                    .sum();
        } else {
            closingBalance = assetStockRepository.findAll().stream()
                    .filter(s -> equipmentType == null || equipmentType.isEmpty()
                            || equipmentType.equals(s.getEquipment().getEquipmentType()))
                    .mapToInt(s -> s.getQuantity())
                    .sum();
        }

        // ── 6. Net Movement = Purchases + Transfer In - Transfer Out ──
        int netMovement = totalPurchases + totalTransferIn - totalTransferOut;

        // ── 7. Opening Balance ──
        int openingBalance;
        if (date != null) {
            // For a selected date range, calculate opening balance from stock & movement records prior to startOfDay
            final Long filterBaseId = baseId;
            final String filterEquipmentType = equipmentType;

            int purchasesBefore = purchaseRepository.findAll().stream()
                    .filter(p -> p.getPurchaseDate().isBefore(startOfDay))
                    .filter(p -> filterBaseId == null || p.getBase().getId().equals(filterBaseId))
                    .filter(p -> filterEquipmentType == null || filterEquipmentType.isEmpty()
                            || filterEquipmentType.equals(p.getEquipment().getEquipmentType()))
                    .mapToInt(Purchase::getQuantity)
                    .sum();

            int transferInBefore = transferRepository.findAll().stream()
                    .filter(t -> t.getTransferDate().isBefore(startOfDay))
                    .filter(t -> filterBaseId == null || t.getToBase().getId().equals(filterBaseId))
                    .filter(t -> filterEquipmentType == null || filterEquipmentType.isEmpty()
                            || filterEquipmentType.equals(t.getEquipment().getEquipmentType()))
                    .mapToInt(Transfer::getQuantity)
                    .sum();

            int transferOutBefore = transferRepository.findAll().stream()
                    .filter(t -> t.getTransferDate().isBefore(startOfDay))
                    .filter(t -> filterBaseId == null || t.getFromBase().getId().equals(filterBaseId))
                    .filter(t -> filterEquipmentType == null || filterEquipmentType.isEmpty()
                            || filterEquipmentType.equals(t.getEquipment().getEquipmentType()))
                    .mapToInt(Transfer::getQuantity)
                    .sum();

            int expendedBefore = expenditureRepository.findAll().stream()
                    .filter(e -> e.getExpenditureDate().isBefore(startOfDay))
                    .filter(e -> filterBaseId == null || e.getBase().getId().equals(filterBaseId))
                    .filter(e -> filterEquipmentType == null || filterEquipmentType.isEmpty()
                            || filterEquipmentType.equals(e.getEquipment().getEquipmentType()))
                    .mapToInt(Expenditure::getQuantity)
                    .sum();

            openingBalance = purchasesBefore + transferInBefore - transferOutBefore - expendedBefore;
        } else {
            // When no date filter is selected, Opening Balance accounts for lifetime expenditures
            // Opening Balance = Closing Balance - Net Movement + Total Expended
            openingBalance = closingBalance - netMovement + totalExpended;
        }

        // Set all values
        response.setOpeningBalance(openingBalance);
        response.setClosingBalance(closingBalance);
        response.setNetMovement(netMovement);
        response.setPurchases(totalPurchases);
        response.setTransferIn(totalTransferIn);
        response.setTransferOut(totalTransferOut);
        response.setAssigned(totalAssigned);
        response.setExpended(totalExpended);

        return response;
    }
}
