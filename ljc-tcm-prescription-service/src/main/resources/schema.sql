-- Database Initialization
-- Create database if not exists
CREATE DATABASE IF NOT EXISTS `ljc_tcm_prescription` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ljc_tcm_prescription`;

-- 1. Herbs Table (药材库)
CREATE TABLE IF NOT EXISTS `herbs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name_cn` VARCHAR(64) NOT NULL COMMENT 'Standard Chinese Name (药材标准名)',
    `unit` VARCHAR(8) NOT NULL DEFAULT 'g' COMMENT 'Measurement Unit (单位)',
    `default_dose_g` DECIMAL(10, 2) NULL COMMENT 'Default Dose in Grams (默认克重)',
    `notes` VARCHAR(255) NULL COMMENT 'Notes (备注)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1: Enabled, 0: Disabled (状态)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` DATETIME NULL COMMENT 'Soft Delete Timestamp (软删除时间)',
    UNIQUE KEY `uk_herbs_name_cn` (`name_cn`),
    KEY `idx_herbs_status` (`status`),
    KEY `idx_herbs_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Herb Library (药材库)';

-- 2. Prescriptions Table (药方)
CREATE TABLE IF NOT EXISTS `prescriptions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(128) NOT NULL COMMENT 'Prescription Name (药方名称)',
    `description` VARCHAR(255) NULL COMMENT 'Description (描述)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` DATETIME NULL COMMENT 'Soft Delete Timestamp (软删除时间)',
    KEY `idx_prescriptions_name` (`name`),
    KEY `idx_prescriptions_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prescriptions (药方表)';

-- 3. Prescription Items Table (药方明细)
CREATE TABLE IF NOT EXISTS `prescription_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `prescription_id` BIGINT NOT NULL COMMENT 'FK to Prescriptions',
    `herb_id` BIGINT NOT NULL COMMENT 'FK to Herbs',
    `herb_name_snapshot` VARCHAR(64) NOT NULL COMMENT 'Snapshot of Herb Name (药材名快照)',
    `dose_g` DECIMAL(10, 2) NOT NULL COMMENT 'Dose in Grams (克重)',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Sort Order (排序)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` DATETIME NULL COMMENT 'Soft Delete Timestamp (软删除时间)',
    UNIQUE KEY `uk_p_h_deleted` (`prescription_id`, `herb_id`, `deleted_at`),
    KEY `idx_pi_prescription_id` (`prescription_id`),
    KEY `idx_pi_herb_id` (`herb_id`),
    CONSTRAINT `fk_pi_prescription` FOREIGN KEY (`prescription_id`) REFERENCES `prescriptions` (`id`),
    CONSTRAINT `fk_pi_herb` FOREIGN KEY (`herb_id`) REFERENCES `herbs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prescription Items (药方明细)';

-- 4. Medical Records Table (病历表)
CREATE TABLE IF NOT EXISTS `medical_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `patient_name` VARCHAR(64) NOT NULL COMMENT 'Patient Name (患者姓名)',
    `visit_date` DATE NOT NULL COMMENT 'Visit Date (就诊日期)',
    `prescription_ids_json` TEXT NOT NULL COMMENT 'Selected Prescription IDs JSON (选中的药方ID列表)',
    `prescription_names_snapshot` VARCHAR(512) NOT NULL COMMENT 'Prescription Names Snapshot (药方名快照，逗号分隔)',
    `merged_herbs_json` MEDIUMTEXT NOT NULL COMMENT 'Merged Herbs Snapshot JSON (合并后的药材清单快照)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` DATETIME NULL COMMENT 'Soft Delete Timestamp (软删除时间)',
    KEY `idx_mr_patient_name` (`patient_name`),
    KEY `idx_mr_visit_date` (`visit_date`),
    KEY `idx_mr_deleted_at` (`deleted_at`),
    KEY `idx_mr_patient_date` (`patient_name`, `visit_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Medical Records (病历表)';
