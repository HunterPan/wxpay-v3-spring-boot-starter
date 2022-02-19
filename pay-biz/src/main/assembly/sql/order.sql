CREATE TABLE `tb_order` (
                            `id` bigint unsigned NOT NULL,
                            `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '描述',
                            `create_time` datetime NOT NULL,
                            `update_time` datetime NOT NULL,
                            `pay_time` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '支付时间',
                            `is_deleted` tinyint unsigned NOT NULL DEFAULT '0',
                            `status` tinyint NOT NULL DEFAULT '0',
                            `total_amount` int unsigned NOT NULL DEFAULT '0' COMMENT '总金额，原金额',
                            `discount_amount` int unsigned NOT NULL DEFAULT '0' COMMENT '优惠金额',
                            `pay_amount` int unsigned NOT NULL DEFAULT '0' COMMENT '支付金额，实付',
                            `payer` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
                            `day` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
                            `out_trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
                            `modify_user` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT ' ',
                            `pay_channel` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '支付渠道',
                            PRIMARY KEY (`id`),
                            KEY `idx_per_ct` (`payer`,`create_time`),
                            KEY `idx_otn` (`out_trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `tb_order_detail` (
                                   `id` bigint unsigned NOT NULL,
                                   `order_id` bigint unsigned NOT NULL COMMENT '订单id',
                                   `item_id` bigint unsigned NOT NULL COMMENT '商品id',
                                   `total_amount` int unsigned NOT NULL DEFAULT '0' COMMENT '总金额，原金额',
                                   `discount_amount` int unsigned NOT NULL DEFAULT '0' COMMENT '优惠金额',
                                   `pay_amount` int unsigned NOT NULL DEFAULT '0' COMMENT '支付金额，实付',
                                   `item_title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品名称',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_oid` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;