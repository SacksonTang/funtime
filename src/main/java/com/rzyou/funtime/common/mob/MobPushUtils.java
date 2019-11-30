package com.rzyou.funtime.common.mob;


import com.rzyou.funtime.common.Constant;
import mob.push.api.MobPushConfig;
import mob.push.api.exception.ApiException;
import mob.push.api.model.PushWork;
import mob.push.api.push.PushClient;
import mob.push.api.utils.AndroidNotifyStyleEnum;
import mob.push.api.utils.PlatEnum;
import mob.push.api.utils.PushTypeEnum;
import mob.push.api.utils.TargetEnum;

public class MobPushUtils {




    public static void mobPush(String content){
        MobPushConfig.appkey = Constant.MOBPUSH_APPID;
        MobPushConfig.appSecret = Constant.MOBPUSH_APPSECRET;

        PushWork push = new PushWork(PlatEnum.all.getCode(),content , PushTypeEnum.notify.getCode()) //初始化基础信息
                .buildTarget(TargetEnum._1.getCode(), null, null, null, null, null)  // 设置推送范围
                .buildAndroid("Android Title", AndroidNotifyStyleEnum.normal.getCode(), null, true, true, true) //定制android样式
                .bulidIos("ios Title", "ios Subtitle", null, 1, null, null, null, null) //定制ios设置
                .buildExtra(10, "{key1:value}", 1) // 设置扩展信息
                ;

        PushClient client = new PushClient();

        try {
            client.sendPush(push);
        } catch (ApiException e) {
            e.printStackTrace();
        }

    }
}






