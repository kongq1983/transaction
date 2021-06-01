
CREATE DATABASE IF NOT EXISTS `test` DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

CREATE TABLE `t_account` (
  `id` bigint(20) NOT NULL,
  `username` varchar(64) NOT NULL,
  `phone` varchar(32) DEFAULT NULL,
  `province` varchar(64) DEFAULT NULL,
  `createtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



