package com.rzyou.funtime.common.agora;

import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.agora.media.RtcTokenBuilder;

public class AgoraTokenUtil {

    public static String getAgoraToken(int uid,String channelName){
        RtcTokenBuilder token = new RtcTokenBuilder();
        String result = token.buildTokenWithUid(Constant.AGORA_APP_ID, Constant.AGORA_APP_CERTIFICATE,
                channelName, uid, RtcTokenBuilder.Role.Role_Publisher, 0);
        return result;
    }


}
