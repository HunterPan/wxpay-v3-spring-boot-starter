CREATE TABLE `tb_pay` (
                           `id` bigint(20) unsigned zerofill NOT NULL COMMENT '流水号',
                           `pay_channel` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '支付渠道',
                           `create_time` datetime NOT NULL,
                           `update_time` datetime NOT NULL,
                           `pay_time` datetime  COMMENT '支付时间',
                           `out_trade_no` String(64)  CHARACTER SET utf8mb4  COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '外部流水号',
                           `is_deleted` tinyint unsigned NOT NULL DEFAULT '0',
                           `status` tinyint NOT NULL DEFAULT '0' comment '状态：1支付中 2成功 3失败 4已退' ,
                           `remark` int unsigned NOT NULL DEFAULT '0' COMMENT '备注',
                           `pay_amount` int unsigned NOT NULL DEFAULT '0' COMMENT '支付金额，实付',
                           `channel_real_amount` int unsigned NOT NULL DEFAULT '0' COMMENT '支付金额，实付',
                           `modify_user` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `tb_channel_pay_return` (
                                         `id` bigint unsigned NOT NULL,
                                         `create_time` datetime NOT NULL,
                                         `update_time` datetime NOT NULL,
                                         `pay_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '支付id',
                                         `out_trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '外部流水号',
                                         `channel_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '渠道返回',
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `idx_pid` (`pay_id`),
                                         KEY `idx_otn` (`out_trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `tb_pay_schedule_job` (
                                       `id` bigint unsigned NOT NULL,
                                       `pay_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '支付id',
                                       `pay_channel` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '支付渠道',
                                       `next_time` datetime NOT NULL,
                                       `create_time` datetime NOT NULL,
                                       `expire_time` datetime NOT NULL,
                                       PRIMARY KEY (`id`),
                                       KEY `idx_nt` (`next_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;