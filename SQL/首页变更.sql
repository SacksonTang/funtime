ALTER TABLE `funtime`.`t_funtime_tag`
ADD COLUMN `tag_url` varchar(255) NULL COMMENT '底图' AFTER `tag_color`;

ALTER TABLE `funtime`.`t_funtime_chatroom`
MODIFY COLUMN `hot` int(8) NULL DEFAULT 0 COMMENT '热门' AFTER `is_lock`,
ADD COLUMN `hots` int(32) NULL COMMENT '热力值' AFTER `hot`;
ALTER TABLE `funtime`.`t_funtime_chatroom`
MODIFY COLUMN `hots` int(32) NULL DEFAULT 0 COMMENT '热力值' AFTER `hot`;