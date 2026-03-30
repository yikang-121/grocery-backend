-- Add new columns for advanced restocking logic
ALTER TABLE inventory_metrics ADD COLUMN incoming_stock INT DEFAULT 0;
ALTER TABLE inventory_metrics ADD COLUMN case_size INT DEFAULT 1;
