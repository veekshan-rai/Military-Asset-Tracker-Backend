-- ==========================================================
-- Military Asset Tracker (MAT) - Stock Reconciliation Migration
-- Updates asset_stock quantities to match historical transaction records
-- Formula per (equipment_id, base_id):
-- Expected Stock = Purchases + Transfer In - Transfer Out - Expenditures
-- ==========================================================

-- Synchronize asset_stock records with transaction history
-- Uses INSERT ... ON DUPLICATE KEY UPDATE to safely update existing rows or insert missing rows
-- preserving the uq_asset_stock_equipment_base unique constraint.

INSERT INTO asset_stock (equipment_id, base_id, quantity)
SELECT 
    eq_base.equipment_id,
    eq_base.base_id,
    GREATEST(0,
        COALESCE(p.total_purchased, 0) 
        + COALESCE(tin.total_transfer_in, 0) 
        - COALESCE(tout.total_transfer_out, 0) 
        - COALESCE(e.total_expended, 0)
    ) AS expected_quantity
FROM (
    SELECT equipment_id, base_id FROM purchases
    UNION SELECT equipment_id, from_base_id FROM transfers
    UNION SELECT equipment_id, to_base_id FROM transfers
    UNION SELECT equipment_id, base_id FROM expenditures
    UNION SELECT equipment_id, base_id FROM asset_stock
) eq_base
LEFT JOIN (
    SELECT equipment_id, base_id, SUM(quantity) AS total_purchased
    FROM purchases GROUP BY equipment_id, base_id
) p ON eq_base.equipment_id = p.equipment_id AND eq_base.base_id = p.base_id
LEFT JOIN (
    SELECT equipment_id, to_base_id AS base_id, SUM(quantity) AS total_transfer_in
    FROM transfers GROUP BY equipment_id, to_base_id
) tin ON eq_base.equipment_id = tin.equipment_id AND eq_base.base_id = tin.base_id
LEFT JOIN (
    SELECT equipment_id, from_base_id AS base_id, SUM(quantity) AS total_transfer_out
    FROM transfers GROUP BY equipment_id, from_base_id
) tout ON eq_base.equipment_id = tout.equipment_id AND eq_base.base_id = tout.base_id
LEFT JOIN (
    SELECT equipment_id, base_id, SUM(quantity) AS total_expended
    FROM expenditures GROUP BY equipment_id, base_id
) e ON eq_base.equipment_id = e.equipment_id AND eq_base.base_id = e.base_id
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);
