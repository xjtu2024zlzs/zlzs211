ALTER TABLE `t3_fault_iden_sample`
    ADD UNIQUE KEY `uk_fault_iden_sample_file` (`condition_label`, `bearing_code`, `file_name`, `data_usage`);

