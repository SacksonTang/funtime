ALTER TABLE `funtime`.`t_funtime_tag`
ADD COLUMN `tag_url` varchar(255) NULL COMMENT '底图' AFTER `tag_color`;

ALTER TABLE `funtime`.`t_funtime_chatroom`
MODIFY COLUMN `hot` int(8) NULL DEFAULT 0 COMMENT '热门' AFTER `is_lock`,
ADD COLUMN `hots` int(32) NULL COMMENT '热力值' AFTER `hot`;
ALTER TABLE `funtime`.`t_funtime_chatroom`
MODIFY COLUMN `hots` int(32) NULL DEFAULT 0 COMMENT '热力值' AFTER `hot`;

ALTER TABLE `funtime`.`t_funtime_user_account_fish_record`
ADD COLUMN `room_id` bigint(64) NULL COMMENT '房间ID' AFTER `user_id`;
ALTER TABLE `funtime`.`t_funtime_user_account_yaoyao_record`
ADD COLUMN `room_id` bigint(64) NULL COMMENT '房间ID' AFTER `user_id`;
ALTER TABLE `funtime`.`t_funtime_user_account_smash_egg_record`
ADD COLUMN `room_id` bigint(64) NULL COMMENT '房间ID' AFTER `user_id`;
ALTER TABLE `funtime`.`t_funtime_user_account_circle_record`
ADD COLUMN `room_id` bigint(64) NULL COMMENT '房间ID' AFTER `user_id`;

INSERT INTO `funtime`.`t_funtime_static_resource`(`id`, `type`, `name`, `url`, `desc`, `create_time`) VALUES (11, 9, '首页活动', 'https://api.rzyou.com/home/h5/home/home_act.html', '首页活动', '2020-06-28 17:01:42');
INSERT INTO `funtime`.`t_funtime_static_resource`(`id`, `type`, `name`, `url`, `desc`, `create_time`) VALUES (12, 10, '宝箱说明', 'https://api.rzyou.com/home/h5/room/gift_box.html', '宝箱说明', '2020-06-28 17:02:14');


update t_funtime_chatroom a set a.hots = (
 select ifnull(sum(if(b.user_role = 1,5,if(u.sex = 2,3,2))),0)
 from t_funtime_chatroom_mic b inner join t_funtime_user u on b.mic_user_id = u.id
 where u.online_state = 1 and b.room_id = a.id
)