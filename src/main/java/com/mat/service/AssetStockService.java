package com.mat.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mat.entity.AssetStock;
import com.mat.entity.Base;
import com.mat.entity.Equipment;
import com.mat.entity.User;
import com.mat.repository.AssetStockRepository;
import com.mat.repository.UserRepository;
import com.mat.security.SecurityUtils;

/**
 * AssetStockService
 *
 * Manages the current stock (AssetStock) records.
 * Provides methods to increase, decrease, and query stock levels.
 */
@Service
public class AssetStockService {

    private final AssetStockRepository assetStockRepository;
    private final UserRepository userRepository;

    public AssetStockService(AssetStockRepository assetStockRepository, UserRepository userRepository) {
        this.assetStockRepository = assetStockRepository;
        this.userRepository = userRepository;
    }

    /**
     * Increase stock for a given equipment at a given base.
     * If no stock record exists, creates one with the given quantity.
     * If a stock record exists, adds the quantity to the existing amount.
     *
     * @param equipment the equipment type
     * @param base      the base
     * @param quantity  the quantity to add (must be > 0)
     */
    public void increaseStock(Equipment equipment, Base base, int quantity) {
        Optional<AssetStock> existing = assetStockRepository.findByEquipmentAndBase(equipment, base);

        if (existing.isPresent()) {
            AssetStock stock = existing.get();
            stock.setQuantity(stock.getQuantity() + quantity);
            assetStockRepository.save(stock);
        } else {
            AssetStock newStock = new AssetStock(equipment, base, quantity);
            assetStockRepository.save(newStock);
        }
    }

    /**
     * Decrease stock for a given equipment at a given base.
     * Validates that the stock record exists and has sufficient quantity.
     *
     * @param equipment the equipment type
     * @param base      the base
     * @param quantity  the quantity to subtract (must be > 0)
     * @throws IllegalStateException if no stock record exists or insufficient stock
     */
    public void decreaseStock(Equipment equipment, Base base, int quantity) {
        AssetStock stock = assetStockRepository.findByEquipmentAndBase(equipment, base)
                .orElseThrow(() -> new IllegalStateException(
                        "No stock record found for " + equipment.getName() + " at " + base.getName() + "."));

        if (stock.getQuantity() < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock for " + equipment.getName() + " at " + base.getName() +
                    ". Available: " + stock.getQuantity() + ", Requested: " + quantity + ".");
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        assetStockRepository.save(stock);
    }

    /**
     * Get the current stock quantity for a given equipment at a given base.
     * Returns 0 if no stock record exists.
     */
    public int getStockQuantity(Equipment equipment, Base base) {
        return assetStockRepository.findByEquipmentAndBase(equipment, base)
                .map(AssetStock::getQuantity)
                .orElse(0);
    }

    /**
     * Retrieves stock records based on user role and optional baseId filter.
     *
     * Role-based data filtering:
     *   - ADMIN: can see stock for all bases (or filtered by baseId if provided)
     *   - BASE_COMMANDER: can see ONLY stock for their assignedBase
     *   - LOGISTICS_OFFICER: existing behavior (can see stock for all bases, or filtered by baseId if provided)
     *
     * @param baseId optional base ID filter
     * @return list of matching AssetStock records
     */
    public List<AssetStock> getStock(Long baseId) {
        if (SecurityUtils.isBaseCommander()) {
            User user = null;
            String username = SecurityUtils.getCurrentUsername();
            if (username != null) {
                user = userRepository.findByUsername(username).orElse(null);
            }
            if (user == null) {
                Long userId = SecurityUtils.getCurrentUserId();
                if (userId != null) {
                    user = userRepository.findById(userId).orElse(null);
                }
            }

            if (user != null && user.getAssignedBase() != null) {
                return assetStockRepository.findByBase_Id(user.getAssignedBase().getId());
            }
            return Collections.emptyList();
        }

        if (baseId != null) {
            return assetStockRepository.findByBase_Id(baseId);
        }
        return assetStockRepository.findAll();
    }

    /**
     * Retrieves stock records for a specific base, enforcing BASE_COMMANDER restriction.
     *
     * @param baseId base ID
     * @return list of matching AssetStock records
     */
    public List<AssetStock> getStockByBase(Long baseId) {
        if (SecurityUtils.isBaseCommander()) {
            User user = null;
            String username = SecurityUtils.getCurrentUsername();
            if (username != null) {
                user = userRepository.findByUsername(username).orElse(null);
            }
            if (user == null) {
                Long userId = SecurityUtils.getCurrentUserId();
                if (userId != null) {
                    user = userRepository.findById(userId).orElse(null);
                }
            }

            if (user != null && user.getAssignedBase() != null) {
                if (user.getAssignedBase().getId().equals(baseId)) {
                    return assetStockRepository.findByBase_Id(baseId);
                } else {
                    return Collections.emptyList();
                }
            }
            return Collections.emptyList();
        }
        return assetStockRepository.findByBase_Id(baseId);
    }
}
