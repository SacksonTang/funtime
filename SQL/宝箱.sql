DROP TABLE IF EXISTS `t_funtime_box_conf`;
CREATE TABLE `t_funtime_box_conf`  (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `box_number` int(16) NULL DEFAULT NULL COMMENT '宝箱编号',
  `draw_type` int(4) NULL DEFAULT NULL COMMENT '中奖类型1-礼物2-蓝钻3-金币4-房间背景5-喇叭6-座驾',
  `draw_id` int(32) NULL DEFAULT NULL COMMENT '中奖礼物ID',
  `probability` int(16) NULL DEFAULT NULL COMMENT '概率',
  `broadcast` int(2) NULL DEFAULT 1 COMMENT '是否播报0-是1-否',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '宝箱中奖配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_funtime_box_conf
-- ----------------------------
INSERT INTO `t_funtime_box_conf` VALUES (3, 1, 1, 146, 6000, 1);
INSERT INTO `t_funtime_box_conf` VALUES (4, 1, 1, 25, 9000, 1);
INSERT INTO `t_funtime_box_conf` VALUES (5, 1, 1, 147, 3000, 1);
INSERT INTO `t_funtime_box_conf` VALUES (6, 1, 1, 3, 1000, 1);
INSERT INTO `t_funtime_box_conf` VALUES (7, 1, 1, 149, 100, 1);
INSERT INTO `t_funtime_box_conf` VALUES (8, 2, 1, 150, 9000, 1);
INSERT INTO `t_funtime_box_conf` VALUES (9, 2, 1, 151, 9000, 1);
INSERT INTO `t_funtime_box_conf` VALUES (10, 2, 1, 152, 6000, 1);
INSERT INTO `t_funtime_box_conf` VALUES (11, 2, 1, 153, 50, 1);
INSERT INTO `t_funtime_box_conf` VALUES (12, 2, 1, 154, 20, 1);

DROP TABLE IF EXISTS `t_funtime_box`;
CREATE TABLE `t_funtime_box`  (
  `box_number` int(16) NOT NULL COMMENT '编号',
  `box_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '箱子名称',
  `original_price` decimal(32, 2) NULL DEFAULT NULL COMMENT '原价',
  `activity_price` decimal(32, 2) NULL DEFAULT NULL COMMENT '活动价',
  `animation_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '动画类型',
  `animation_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '动画地址',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '缩略图地址',
  `flag` int(2) NULL DEFAULT 1 COMMENT '1-有效2-无效',
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`box_number`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '宝箱表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_funtime_box
-- ----------------------------
INSERT INTO `t_funtime_box` VALUES (1, '心愿宝箱', 20.00, 20.00, 'SVGA', 'https://funtime-1300805214.cos.ap-shanghai.myqcloud.com/box/xinyuan', 'https://funtime-1300805214.cos.ap-shanghai.myqcloud.com/box/xinyuan.png', 1, '2020-06-12 21:10:50');
INSERT INTO `t_funtime_box` VALUES (2, '梦幻宝箱', 100.00, 100.00, 'SVGA', 'https://funtime-1300805214.cos.ap-shanghai.myqcloud.com/box/menghuan', 'https://funtime-1300805214.cos.ap-shanghai.myqcloud.com/box/menghuan.png', 1, '2020-06-12 21:10:52');
