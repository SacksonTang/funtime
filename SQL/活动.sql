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
   `activity_limit` int(32) DEFAULT NULL COMMENT '限制人数',
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


INSERT INTO `funtime`.`t_funtime_game_circle_activity_conf`(`id`, `draw_number`, `name`, `draw_url`, `draw_type`, `draw_id`, `draw_val`, `probability`) VALUES (1, 9, NULL, NULL, 1, 25, NULL, 689);
INSERT INTO `funtime`.`t_funtime_game_circle_activity_conf`(`id`, `draw_number`, `name`, `draw_url`, `draw_type`, `draw_id`, `draw_val`, `probability`) VALUES (2, 8, NULL, NULL, 1, 74, NULL, 200);
INSERT INTO `funtime`.`t_funtime_game_circle_activity_conf`(`id`, `draw_number`, `name`, `draw_url`, `draw_type`, `draw_id`, `draw_val`, `probability`) VALUES (3, 7, NULL, NULL, 1, 91, NULL, 50);
INSERT INTO `funtime`.`t_funtime_game_circle_activity_conf`(`id`, `draw_number`, `name`, `draw_url`, `draw_type`, `draw_id`, `draw_val`, `probability`) VALUES (4, 6, NULL, NULL, 1, 37, NULL, 20);
INSERT INTO `funtime`.`t_funtime_game_circle_activity_conf`(`id`, `draw_number`, `name`, `draw_url`, `draw_type`, `draw_id`, `draw_val`, `probability`) VALUES (5, 5, NULL, NULL, 1, 57, NULL, 10);
INSERT INTO `funtime`.`t_funtime_game_circle_activity_conf`(`id`, `draw_number`, `name`, `draw_url`, `draw_type`, `draw_id`, `draw_val`, `probability`) VALUES (6, 4, NULL, NULL, 1, 58, NULL, 1);
INSERT INTO `funtime`.`t_funtime_game_circle_activity_conf`(`id`, `draw_number`, `name`, `draw_url`, `draw_type`, `draw_id`, `draw_val`, `probability`) VALUES (7, 3, NULL, NULL, 1, 56, NULL, 0);
INSERT INTO `funtime`.`t_funtime_game_circle_activity_conf`(`id`, `draw_number`, `name`, `draw_url`, `draw_type`, `draw_id`, `draw_val`, `probability`) VALUES (8, 2, NULL, NULL, 1, 87, NULL, 0);
INSERT INTO `funtime`.`t_funtime_game_circle_activity_conf`(`id`, `draw_number`, `name`, `draw_url`, `draw_type`, `draw_id`, `draw_val`, `probability`) VALUES (9, 1, NULL, NULL, 1, 67, NULL, 0);
INSERT INTO `funtime`.`t_funtime_game_circle_activity_conf`(`id`, `draw_number`, `name`, `draw_url`, `draw_type`, `draw_id`, `draw_val`, `probability`) VALUES (10, 12, NULL, NULL, 1, 61, NULL, 0);
INSERT INTO `funtime`.`t_funtime_game_circle_activity_conf`(`id`, `draw_number`, `name`, `draw_url`, `draw_type`, `draw_id`, `draw_val`, `probability`) VALUES (11, 11, NULL, NULL, 1, 53, NULL, 0);
INSERT INTO `funtime`.`t_funtime_game_circle_activity_conf`(`id`, `draw_number`, `name`, `draw_url`, `draw_type`, `draw_id`, `draw_val`, `probability`) VALUES (12, 10, NULL, NULL, 1, 17, NULL, 0);

CREATE TABLE `t_funtime_user_activity` (
  `user_id` bigint(64) NOT NULL,
  `activity_id` int(16) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户渠道表';
CREATE TABLE `t_funtime_activity_enum` (
  `id` int(16) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `activity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '渠道名称',
  `activity_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '渠道账号',
  `activity_state` int(2) DEFAULT NULL COMMENT '渠道状态1-启用2-禁用',
  `create_user_id` int(16) DEFAULT NULL COMMENT '创建人',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `activity_number` (`activity_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='渠道表';
ALTER TABLE `funtime`.`t_funtime_channel`
ADD COLUMN `create_user_id` int(16) NULL COMMENT '创建人' AFTER `channel_state`;
ALTER TABLE `funtime`.`t_funtime_channel`
ADD UNIQUE INDEX `channel_index`(`channel_number`) USING BTREE;