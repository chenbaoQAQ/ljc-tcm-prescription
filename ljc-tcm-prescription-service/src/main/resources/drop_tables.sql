-- Drop all tables script
-- Use this to clean up the database completely before re-running schema.sql
USE `ljc_tcm_prescription`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `medical_records`;
DROP TABLE IF EXISTS `prescription_items`;
DROP TABLE IF EXISTS `prescriptions`;
DROP TABLE IF EXISTS `herbs`;

SET FOREIGN_KEY_CHECKS = 1;
