CREATE TABLE `t_funtime_activity` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `activity_no` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '活动编号',
  `activity_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '活动名称',
  `channel_no` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '渠道编号',
  `channel_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '渠道名称',
  `draw_type` int(4) DEFAULT NULL COMMENT '1-礼物2-蓝钻',
  `user_start_time` timestamp NULL DEFAULT NULL COMMENT '用户注册开始时间',
  `user_end_time` timestamp NULL DEFAULT NULL COMMENT '用户注册结束时间',
  `activity_start_time` timestamp NULL DEFAULT NULL COMMENT '活动开始时间',
  `activity_end_time` timestamp NULL DEFAULT NULL COMMENT '活动结束时间',
  `create_user_id` int(16) DEFAULT NULL COMMENT '操作人',
  `flag` int(2) DEFAULT '1' COMMENT '1-有效2-无效',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `activity_no` (`activity_no`,`channel_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='活动表';

CREATE TABLE `t_funtime_game_circle_activity_conf` (
  `id` int(16) NOT NULL AUTO_INCREMENT,
  `draw_number` int(8) NOT NULL COMMENT '数字',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '奖品名字',
  `draw_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图片URL',
  `draw_type` int(2) NOT NULL DEFAULT '2' COMMENT '中奖类型1-礼物2-蓝钻3-金币4-房间背景5-喇叭6-座驾',
  `draw_id` int(16) DEFAULT NULL COMMENT '中奖奖品ID',
  `draw_val` int(32) DEFAULT NULL COMMENT '中奖蓝钻数/金币数',
  `probability` int(16) NOT NULL COMMENT '概率',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='活动转盘配置表';

CREATE TABLE `t_funtime_user_account_circle_activity_record` (
  `id` bigint(64) NOT NULL AUTO_INCREMENT,
  `activity_id` int(16) DEFAULT NULL COMMENT '活动ID',
  `user_id` bigint(64) DEFAULT NULL COMMENT '用户ID',
  `draw_random` int(32) DEFAULT NULL COMMENT '抽中的随机数',
  `draw_number` int(8) NOT NULL COMMENT '数字',
  `draw_type` int(2) NOT NULL DEFAULT '2' COMMENT '中奖类型1-礼物2-蓝钻3-金币4-房间背景5-喇叭',
  `draw_id` int(32) DEFAULT NULL COMMENT '中奖奖品ID',
  `draw_val` int(32) DEFAULT NULL COMMENT '中奖蓝钻数/金币数',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `activity_id` (`activity_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=470 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='活动转盘记录表';