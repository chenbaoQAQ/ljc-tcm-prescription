-- Add notes column to medical_records table
-- Run this if the table already exists and doesn't have the notes column

ALTER TABLE `medical_records` 
ADD COLUMN `notes` TEXT NULL COMMENT 'Notes (备注)' 
AFTER `merged_herbs_json`;
