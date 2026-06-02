ALTER TABLE `fault_iden_sample`
    ADD COLUMN `aircraft_id` VARCHAR(50) NULL COMMENT 'aircraft id' AFTER `file_size`,
    ADD COLUMN `subsystem_id` VARCHAR(50) NULL COMMENT 'subsystem id' AFTER `aircraft_id`,
    ADD COLUMN `equipment_id` VARCHAR(50) NULL COMMENT 'equipment id' AFTER `subsystem_id`,
    ADD COLUMN `component_id` VARCHAR(50) NULL COMMENT 'component id' AFTER `equipment_id`,
    ADD KEY `idx_fault_iden_sample_aircraft_id` (`aircraft_id`),
    ADD KEY `idx_fault_iden_sample_subsystem_id` (`subsystem_id`),
    ADD KEY `idx_fault_iden_sample_equipment_id` (`equipment_id`),
    ADD KEY `idx_fault_iden_sample_component_id` (`component_id`);
