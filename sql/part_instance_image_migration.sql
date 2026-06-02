SET NAMES utf8mb4;

ALTER TABLE `part_instances`
  ADD COLUMN `image_url` varchar(255) DEFAULT NULL COMMENT '零件图片地址' AFTER `key_degree`;
