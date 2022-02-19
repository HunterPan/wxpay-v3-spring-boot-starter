CREATE TABLE `tb_item` (
                           `id` bigint unsigned NOT NULL,
                           `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品名称',
                           `price` int unsigned NOT NULL DEFAULT '0' COMMENT '价格',
                           `discount` int unsigned NOT NULL DEFAULT '0' COMMENT 'par',
                           `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '描述',
                           `create_time` datetime NOT NULL,
                           `update_time` datetime NOT NULL,
                           `is_deleted` tinyint unsigned NOT NULL DEFAULT '0',
                           `status` tinyint NOT NULL DEFAULT '0',
                           `modify_user` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
                           `category` int unsigned NOT NULL DEFAULT '1' COMMENT '1 卡 2积分',
                           PRIMARY KEY (`id`),
                           KEY `idx` (`status`,`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;