CREATE DATABASE IF NOT EXISTS `imagehost` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `imagehost`;

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `images`;
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) DEFAULT 'USER',
  `enabled` bit(1) NOT NULL DEFAULT b'1',
  `quota_space` bigint(20) DEFAULT '10485760', -- Default 10MB
  `quota_count` int(11) DEFAULT '1', -- Default 1 image
  `used_space` bigint(20) DEFAULT '0',
  `used_count` int(11) DEFAULT '0',
  `disabled_until` datetime DEFAULT NULL,
  `disable_reason` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `images` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `original_name` varchar(255) DEFAULT NULL,
  `alias` varchar(255) DEFAULT NULL,
  `url` varchar(500) DEFAULT NULL,
  `md5` varchar(32) DEFAULT NULL,
  `size` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_user_id` (`user_id`),
  KEY `IDX_url` (`url`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS=1;
