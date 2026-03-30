-- Add missing seasonality_factor column to inventory_metrics table
ALTER TABLE inventory_metrics ADD COLUMN seasonality_factor DOUBLE DEFAULT 1.0;
