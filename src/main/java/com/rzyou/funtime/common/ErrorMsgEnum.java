package com.rzyou.funtime.common;

public enum ErrorMsgEnum {

    SUCCESS("0","成功"),
    PARAMETER_ERROR("99999","获取参数失败"),
    PARAMETER_CONF_ERROR("99998","获取配置参数失败"),
    PARAMETER_DECRYPT_ERROR("99997","参数解密失败"),
    PARAMETER_ENCRYPT_ERROR("99996","参数加密失败"),
    UNKNOWN_ERROR("10000","系统繁忙,请稍后再试...."),
    DATA_ORER_ERROR("10001","数据操作失败"),
    USER_NOT_EXISTS("10002","用户不存在"),
    USER_IS_DELETE("10003","用户已封禁"),
    USER_IS_NORMAL("10004","用户已经正常"),
    USER_LOGIN_ERROR("10005","用户登录失败"),
    USER_TOKEN_ERROR("10006","用户令牌token过期或无效"),
    USER_TOKEN_EMPTY("10007","用户令牌token为空"),
    USER_TOKEN_EXPIRE("10008","用户令牌token过期"),
    USER_ID_NOT_EXIST("10009","USERID为空"),
    USER_GETTOKEN_FAIL("10010","获取token失败"),
    USER_SMS_FAIL("10011","短信发送失败"),
    USER_LOGINTYPE_ERROR("10012","登录类型LOGINTYPE错误"),
    USER_ACCOUNT_BLACK_NOT_EN("10013","账户红钻不足"),
    USER_ACCOUNT_BLUE_NOT_EN("10014","账户蓝钻不足,请及时充值"),
    USER_ACCOUNT_HORN_NOT_EN("10015","账户喇叭不足,请及时充值"),
    PHONE_NUMBER_IS_REGISTER("10016","手机号已被注册"),
    USERAGREEMENT_IS_EXISTS("10017","用户已经同意该协议"),
    USERVALID_IS_EXISTS("10018","用户已认证"),
    USERVALID_IS_NOT_VALID("10019","用户未认证"),
    USERAGREEMENT_IS_NOT_EXISTS("10020","用户未同意协议"),
    USERCONCERN_IS_EXISTS("10021","已关注该用户"),
    USERCONCERN_IS_NOT_EXISTS("10022","未关注该用户"),
    USER_WXLOGIN_TOKEN_ERROR("10023","微信登录获取access_token失败"),
    USER_WXLOGIN_VALIDTOKEN_ERROR("10024","微信登录检测access_token失败"),
    USER_WXLOGIN_REFRESHTOKEN_ERROR("10025","微信登录刷新access_token失败"),
    USER_WXLOGIN_USERINFO_ERROR("10026","微信登录获取用户信息失败"),
    USER_SYNC_TENCENT_ERROR("10027","同步腾讯接口出错"),
    USER_LOGIN_ONEKEY_ERROR("10028","运营商故障,请使用切换号码登录"),
    USER_GETCREDENTIAL_ERROR("10029","获取临时密钥失败"),

    USER_QQLOGIN_GETOPENID_ERROR("10030","QQ登录获取openId失败"),
    USER_QQLOGIN_GETUSERINFO_ERROR("10031","QQ登录获取用户信息失败"),
    USER_QQLOGIN_GETTOKEN_ERROR("10032","QQ登录获取token失败"),

    USER_BANKCARD_VALID_ERROR("10033","银行卡验证失败"),
    PHONE_NUMBER_IS_NOT_REGISTER("10034","手机号不存在"),

    USER_GETOPENID_ERROR("10035","获取openid失败"),
    USER_NOT_REALNAME_VALID("10036","用户未实名认证"),
    USER_ACCOUNT_GOLD_NOT_EN("10037","账户金币不足"),
    USER_WX_EXISTS("10038","该微信已被其它账号绑定"),
    USER_WX_NOT_BIND("10039","没有用户微信绑定信息"),
    USER_IS_OFFLINE("10040","用户IM已离线"),
    USER_IS_LOGIN_OTHER("10041","你已在别的地方上线"),
    USER_BULLET_NO_EN("10042","子弹数不足"),
    USER_APPLELOGIN_ERROR("10043","苹果登录失败"),
    USER_BAG_NOT_EN("10044","用户背包礼物不足"),
    USER_CAR_NOT_EXIST("10045","用户无此坐骑"),
    USER_PHONE_NOT_BIND("10046","请先绑定手机号"),
    USER_VALID_CARD_SAME("10047","银行卡已存在"),
    USER_SIGN_ERROR("10048","已签到"),
    USER_MANAGER_ERROR("10049","小可爱,不能添加自己为管理员喔"),
    USER_QQ_NOT_BIND("10050","没有用户QQ绑定信息"),
    USER_QQ_EXISTS("10051","该QQ已被其它账号绑定"),
    USER_HEADWEAR_NOT_EXIST("10052","用户无此头饰"),
    USER_NICKNAME_ERROR("10053","用户昵称不能带有触娱或官方"),
    USER_MUSIC_NOT_EXIST("10054","音乐不存在,请刷新重试"),
    USER_MUSIC_TAG_EXIST("10055","已经有重复的名字了,请换一个吧~"),
    USER_MUSIC_TAG_LIMIT("10056","标签数量最多只能50个"),

    SMS_NOT_EXISTS("20000","验证码不存在"),
    SMS_IS_USED("20001","验证码已使用"),
    SMS_IS_EXPIRE("20002","验证码已过期"),

    UNIFIELDORDER_ERROR("30001","微信支付统一下单接口调用失败"),
    VALID_SIGN_ERROR("30002","微信支付支付回调校验签名失败"),
    TOTAL_FEE_ERROR("30003","totalFee参数必须大于0"),
    ORDERQUERY_ERROR("30004","微信支付订单查询失败"),
    MMPAYMKTTRANSFER_ERROR("30005","企业付款调用失败"),
    IOSPAY_VALID_ERROR("30006","苹果验证失败,返回数据为空"),
    IOSPAY_NOT_THIS("30007","当前交易不在交易列表中"),
    IOSPAY_TRANSFERS_EMPTY("30008","未能获取获取到交易列表"),
    IOSPAY_ERROR("30009","支付失败,错误码："),
    MMPAYMKTTRANSFER_NOTENOUGH("30010","企业账户余额不足"),
    MMPAYMKTTRANSFER_SIMPLE_BAN("30011","用户微信支付账户未实名,无法付款"),
    ALIPAY_ERROR("30012","支付失败"),
    ALIPAY_QUERY_ERROR("30013","ALIPAY查询订单失败"),

    ORDER_NOT_EXISTS("40001","充值订单记录不存在"),
    ORDER_IS_INVALID("40002","订单记录已失效"),
    ORDER_DATE_ERROR("40003","日期格式有误"),
    RECHARGE_CONF_NOT_EXISTS("40004","充值配置不存在"),
    RECHARGE_NUM_OUT("40005","充值超出最大值"),
    RECHARGE_RMB_NOT_SAME("40006","金额不一致"),
    RECHARGE_LEVEL_NOT_EXISTS("40007","充值等级配置不存在"),
    RECHARGE_TRANSACTIONID_EXISTS("40008","transactionId已存在"),

    REDPACKET_IS_OVER("50001","红包已经抢完"),
    REDPACKET_IS_GRABED("50002","亲,您已经抢过,请点其他红包,谢谢"),
    REDPACKET_IS_NOT_EXISTS("50003","红包不存在"),
    REDPACKET_IS_NOT_SELF("50004","自己不能给自己发"),
    REDPACKET_IS_NOT_YOURS("50005","没有权限抢该红包"),
    REDPACKET_IS_EMPIRE("50006","红包已过期"),
    REDPACKET_NUM_ERROR("50007","红包个数必须大于等于1"),
    REDPACKET_AMOUNT_ERROR("50008","蓝钻数必须大于等于"),
    REDPACKET_AMOUNT_NUM_ERROR("50009","红包个数必须小于等于蓝钻数"),
    REDPACKET_MAXAMOUNT_ERROR("50010","蓝钻数必须小于等于"),

    GIFT_NOT_EXISTS("60001","礼物不存在"),
    BOX_NOT_EXISTS("60002","宝箱不存在"),

    WITHDRAWAL_DAY_LIMIT("70001","当日已达到最高提现金额"),
    WITHDRAWAL_MONTH_LIMIT("70002","已达到每月最高提现次数"),
    WITHDRAWAL_MIN_LIMIT("70003","未达到最低提现要求,微信要求最低提现#元(@钻)"),
    WITHDRAWAL_OPERATION_LIMIT("70004","有处理中申请,待处理完成后再提交,请移步公众号查询进度"),
    WITHDRAWAL_CHANNELAMOUNT_ERROR("70005","渠道费用不符,请重试"),
    WITHDRAWAL_PRERMBAMOUNT_ERROR("70006","试算金额不符,请重试"),
    WITHDRAWAL_RMBAMOUNT_ERROR("70007","实际提现金额不符,请重试"),
    WITHDRAWAL_WX_NOT_BIND("70008","微信没有绑定"),
    WITHDRAWAL_PRERMBAMOUNT_100_ERROR("70009","提现\"提现红钻\"必须为10的倍数"),
    WITHDRAWAL_PHONE_NOT_BIND("70010","手机没有绑定"),
    WITHDRAWAL_QQ_NOT_BIND("70011","QQ没有绑定"),

    ROOM_CREATE_ERROR("80001","房间创建失败"),
    ROOM_NOT_EXISTS("80002","房间不存在"),
    ROOM_IS_BLOCK("80003","房间已经封禁"),
    ROOM_JOIN_PASS_ERROR("80004","房间密码错误"),
    ROOM_JOIN_PASS_EMPTY("80005","房间密码为空,请输入密码"),
    ROOM_JOIN_USER_BLOCKED("80006","您已被踢出,请10分钟后再入"),
    ROOM_JOIN_USER_EXISTS("80007","您已加入该房间"),
    ROOM_EXIT_USER_NOT_EXISTS("80008","您已退出该房间"),
    ROOM_KICKED_USER_EXIST("80009","10分钟内您已踢过该用户"),
    ROOM_MIC_USER_EXIST("80010","麦位已经有人"),
    ROOM_MIC_LOCATION_NOT_EXIST("80011","无此麦位"),
    ROOM_MIC_USER_NOT_EXIST("80012","麦位没有此人"),
    ROOM_JOIN_BUSY("800013","系统繁忙,请重新加入"),
    ROOM_MIC_NO_AUTH("80014","用户没有权限封麦"),
    ROOM_CREATER_ERROR("80015","只有房间创建者可以设置房间信息"),
    ROOM_CLOSE_NO_AUTH("80016","只有房间创建者可以关闭房间"),
    ROOM_CREATE_TENCENT_ERROR("80017","腾讯同步创建聊天室失败"),
    ROOM_CREATE_OUT_ERROR("80018","每个用户只能创建一个房间"),
    ROOM_USER_NO_AUTH("80019","用户无此权限"),
    ROOM_MIC_IS_STOP("80020","麦位已封麦"),
    ROOM_MIC_IS_MANAGE("80021","已经是主持"),
    ROOM_MIC_IS_NOT_MANAGE("80022","不是主持"),
    ROOM_JOIN_TENCENT_ERROR("80023","同步腾讯添加用户失败"),
    ROOM_IS_CLOSE("80024","房间已停播"),
    ROOM_MIC_IS_EXIST("80025","你已在麦上"),
    ROOM_USER_IS_EMPTY("80026","房间没有其他人"),
    ROOM_MICUSER_IS_EMPTY("80027","房间麦上没有其他人"),
    ROOM_BACKGROUND_ERROR("80028","无此背景或已过期"),
    ROOM_BACKGROUND_NOBUY("80029","该资源免费,不需要购买"),
    ROOM_LOWER_OWER_ERROR("80030","房主不能下麦"),
    ROOM_GAME21_MIC_EMPTY("80031","麦位无人"),
    ROOM_GAME21_EXISTS("80032","游戏正在进行中，需要游戏结束后才能发起"),
    ROOM_GAME21_MIC_ERROR("80033","麦位参数有误"),
    ROOM_GAME21_STATE_ERROR("80034","你已要牌"),
    ROOM_GAME21_UPPER_ERROR("80035","游戏中,不能跳麦"),
    ROOM_GAME123_EXISTS("80036","游戏正在进行中，请等待游戏结束后再发起"),
    ROOM_GAME123_AUTH("80037","无此权限"),
    ROOM_GAME123_NOT_EXISTS("80038","无此游戏"),

    DRAW_POOL_NOT_EN("90001","奖池余额不足"),
    DRAW_TIME_OUT("90002","该活动已结束,敬请期待下次开启"),
    DRAW_ACTIVITY_USER_EXIST("90003","活动已经参与过，不能重复参与"),
    DRAW_ACTIVITY_EMPIRE("90004","本活动仅对注册x小时内用户开放喔~"),
    DRAW_ACTIVITY_ID_ERROR("90005","触娱ID错误，请进入触娱App-我的-触娱ID中复制")

    ;

    private String value;
    private String desc;

    ErrorMsgEnum(String value, String desc) {
        this.setValue(value);
        this.setDesc(desc);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "[" + this.value + "]" + this.desc;
    }

}
