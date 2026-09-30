package com.mat.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;

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

/**
 * StockReconciliationService
 *
 * Calculates expected stock per equipment and per base combination:
 *   Expected Stock = Initial Stock (0) + Purchases + Transfer In - Transfer Out - Expenditures
 *
 * Compares Expected Stock against current asset_stock table records to produce
 * a reconciliation report and a safe SQL migration script to synchronize quantities.
 */
@Service
public class StockReconciliationService {

    private final PurchaseRepository purchaseRepository;
    private final TransferRepository transferRepository;
    private final ExpenditureRepository expenditureRepository;
    private final AssetStockRepository assetStockRepository;
    private final BaseRepository baseRepository;
    private final EquipmentRepository equipmentRepository;

    public StockReconciliationService(PurchaseRepository purchaseRepository,
                                     TransferRepository transferRepository,
                                     ExpenditureRepository expenditureRepository,
                                     AssetStockRepository assetStockRepository,
                                     BaseRepository baseRepository,
                                     EquipmentRepository equipmentRepository) {
        this.purchaseRepository = purchaseRepository;
        this.transferRepository = transferRepository;
        this.expenditureRepository = expenditureRepository;
        this.assetStockRepository = assetStockRepository;
        this.baseRepository = baseRepository;
        this.equipmentRepository = equipmentRepository;
    }

    public static class ReconciliationItem {
        private final Long equipmentId;
        private final String equipmentName;
        private final Long baseId;
        private final String baseName;
        private final int currentStock;
        private final int expectedStock;
        private final int difference;

        public ReconciliationItem(Long equipmentId, String equipmentName, Long baseId, String baseName,
                                  int currentStock, int expectedStock) {
            this.equipmentId = equipmentId;
            this.equipmentName = equipmentName;
            this.baseId = baseId;
            this.baseName = baseName;
            this.currentStock = currentStock;
            this.expectedStock = expectedStock;
            this.difference = expectedStock - currentStock;
        }

        public Long getEquipmentId() { return equipmentId; }
        public String getEquipmentName() { return equipmentName; }
        public Long getBaseId() { return baseId; }
        public String getBaseName() { return baseName; }
        public int getCurrentStock() { return currentStock; }
        public int getExpectedStock() { return expectedStock; }
        public int getDifference() { return difference; }

        @Override
        public String toString() {
            return String.format("Equipment: %s (ID: %d), Base: %s (ID: %d) | Current: %d, Expected: %d, Diff: %+d",
                    equipmentName, equipmentId, baseName, baseId, currentStock, expectedStock, difference);
        }
    }

    private static class EquipmentBasePair {
        private final Long equipmentId;
        private final Long baseId;

        public EquipmentBasePair(Long equipmentId, Long baseId) {
            this.equipmentId = equipmentId;
            this.baseId = baseId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EquipmentBasePair pair = (EquipmentBasePair) o;
            return Objects.equals(equipmentId, pair.equipmentId) && Objects.equals(baseId, pair.baseId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(equipmentId, baseId);
        }
    }

    /**
     * Generates a reconciliation report comparing calculated expected stock against asset_stock table.
     */
    public List<ReconciliationItem> generateReconciliationReport() {
        List<Purchase> purchases = purchaseRepository.findAll();
        List<Transfer> transfers = transferRepository.findAll();
        List<Expenditure> expenditures = expenditureRepository.findAll();
        List<AssetStock> currentStocks = assetStockRepository.findAll();

        Map<Long, Equipment> equipmentMap = new HashMap<>();
        Map<Long, Base> baseMap = new HashMap<>();

        for (Equipment e : equipmentRepository.findAll()) {
            equipmentMap.put(e.getId(), e);
        }
        for (Base b : baseRepository.findAll()) {
            baseMap.put(b.getId(), b);
        }

        Set<EquipmentBasePair> pairs = new HashSet<>();

        // Collect pairs from transactions
        for (Purchase p : purchases) {
            if (p.getEquipment() != null && p.getBase() != null) {
                pairs.add(new EquipmentBasePair(p.getEquipment().getId(), p.getBase().getId()));
                equipmentMap.put(p.getEquipment().getId(), p.getEquipment());
                baseMap.put(p.getBase().getId(), p.getBase());
            }
        }
        for (Transfer t : transfers) {
            if (t.getEquipment() != null) {
                if (t.getFromBase() != null) {
                    pairs.add(new EquipmentBasePair(t.getEquipment().getId(), t.getFromBase().getId()));
                    baseMap.put(t.getFromBase().getId(), t.getFromBase());
                }
                if (t.getToBase() != null) {
                    pairs.add(new EquipmentBasePair(t.getEquipment().getId(), t.getToBase().getId()));
                    baseMap.put(t.getToBase().getId(), t.getToBase());
                }
                equipmentMap.put(t.getEquipment().getId(), t.getEquipment());
            }
        }
        for (Expenditure e : expenditures) {
            if (e.getEquipment() != null && e.getBase() != null) {
                pairs.add(new EquipmentBasePair(e.getEquipment().getId(), e.getBase().getId()));
                equipmentMap.put(e.getEquipment().getId(), e.getEquipment());
                baseMap.put(e.getBase().getId(), e.getBase());
            }
        }
        for (AssetStock s : currentStocks) {
            if (s.getEquipment() != null && s.getBase() != null) {
                pairs.add(new EquipmentBasePair(s.getEquipment().getId(), s.getBase().getId()));
                equipmentMap.put(s.getEquipment().getId(), s.getEquipment());
                baseMap.put(s.getBase().getId(), s.getBase());
            }
        }

        // Map current stock
        Map<EquipmentBasePair, Integer> currentStockMap = new HashMap<>();
        for (AssetStock s : currentStocks) {
            if (s.getEquipment() != null && s.getBase() != null) {
                currentStockMap.put(new EquipmentBasePair(s.getEquipment().getId(), s.getBase().getId()), s.getQuantity());
            }
        }

        List<ReconciliationItem> report = new ArrayList<>();

        for (EquipmentBasePair pair : pairs) {
            Long eqId = pair.equipmentId;
            Long bId = pair.baseId;

            int totalPurchases = purchases.stream()
                    .filter(p -> p.getEquipment() != null && p.getEquipment().getId().equals(eqId))
                    .filter(p -> p.getBase() != null && p.getBase().getId().equals(bId))
                    .mapToInt(Purchase::getQuantity)
                    .sum();

            int totalTransferIn = transfers.stream()
                    .filter(t -> t.getEquipment() != null && t.getEquipment().getId().equals(eqId))
                    .filter(t -> t.getToBase() != null && t.getToBase().getId().equals(bId))
                    .mapToInt(Transfer::getQuantity)
                    .sum();

            int totalTransferOut = transfers.stream()
                    .filter(t -> t.getEquipment() != null && t.getEquipment().getId().equals(eqId))
                    .filter(t -> t.getFromBase() != null && t.getFromBase().getId().equals(bId))
                    .mapToInt(Transfer::getQuantity)
                    .sum();

            int totalExpenditure = expenditures.stream()
                    .filter(e -> e.getEquipment() != null && e.getEquipment().getId().equals(eqId))
                    .filter(e -> e.getBase() != null && e.getBase().getId().equals(bId))
                    .mapToInt(Expenditure::getQuantity)
                    .sum();

            int rawExpected = totalPurchases + totalTransferIn - totalTransferOut - totalExpenditure;
            int expectedStock = Math.max(0, rawExpected);
            int currentStock = currentStockMap.getOrDefault(pair, 0);

            Equipment eq = equipmentMap.get(eqId);
            Base b = baseMap.get(bId);

            String eqName = (eq != null && eq.getName() != null) ? eq.getName() : "Equipment #" + eqId;
            String bName = (b != null && b.getName() != null) ? b.getName() : "Base #" + bId;

            report.add(new ReconciliationItem(eqId, eqName, bId, bName, currentStock, expectedStock));
        }

        return report;
    }

    /**
     * Generates a safe SQL migration script to update asset_stock quantities to calculated expected values.
     */
    public String generateSqlMigrationScript() {
        List<ReconciliationItem> report = generateReconciliationReport();
        StringBuilder sql = new StringBuilder();
        sql.append("-- ==========================================================\n");
        sql.append("-- Stock Reconciliation SQL Migration Script\n");
        sql.append("-- Updates asset_stock quantities to match historical transactions\n");
        sql.append("-- ==========================================================\n\n");

        for (ReconciliationItem item : report) {
            if (item.getDifference() != 0) {
                sql.append(String.format(
                        "-- Synchronizing Equipment '%s' (ID: %d) at Base '%s' (ID: %d): current=%d -> expected=%d (Diff: %+d)\n",
                        item.getEquipmentName(), item.getEquipmentId(), item.getBaseName(), item.getBaseId(),
                        item.getCurrentStock(), item.getExpectedStock(), item.getDifference()
                ));
                sql.append(String.format(
                        "INSERT INTO asset_stock (equipment_id, base_id, quantity) VALUES (%d, %d, %d) ON DUPLICATE KEY UPDATE quantity = %d;\n\n",
                        item.getEquipmentId(), item.getBaseId(), item.getExpectedStock(), item.getExpectedStock()
                ));
            }
        }
        return sql.toString();
    }
}
