package com.rzyou.funtime.common;

public enum ErrorMsgEnum {

    SUCCESS("0","成功"),
    PARAMETER_ERROR("99999","获取参数失败"),
    PARAMETER_CONF_ERROR("99998","获取配置参数失败"),
    UNKNOWN_ERROR("10000","系统繁忙，请稍后再试...."),
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
    USER_ACCOUNT_BLACK_NOT_EN("10013","账户黑钻不足,请及时充值"),
    USER_ACCOUNT_BLUE_NOT_EN("10014","账户蓝钻不足,请及时充值"),
    USER_ACCOUNT_HORN_NOT_EN("10015","账户喇叭不足,请及时充值"),
    PHONE_NUMBER_IS_REGISTER("10016","手机号已被注册"),
    USERAGREEMENT_IS_EXISTS("10017","用户已经同意该协议"),
    USERVALID_IS_EXISTS("10018","用户已经同意该协议"),
    USERVALID_IS_NOT_VALID("10019","用户未认证"),
    USERAGREEMENT_IS_NOT_EXISTS("10020","用户未同意协议"),
    USERCONCERN_IS_EXISTS("10021","已关注该用户"),
    USERCONCERN_IS_NOT_EXISTS("10022","未关注该用户"),
    USER_WXLOGIN_TOKEN_ERROR("10023","微信登录获取access_token失败"),
    USER_WXLOGIN_VALIDTOKEN_ERROR("10024","微信登录检测access_token失败"),
    USER_WXLOGIN_REFRESHTOKEN_ERROR("10025","微信登录刷新access_token失败"),
    USER_WXLOGIN_USERINFO_ERROR("10026","微信登录获取用户信息失败"),
    USER_SYNC_TENCENT_ERROR("10027","同步腾讯接口出错"),
    USER_LOGIN_ONEKEY_ERROR("10028","一键登录失败"),
    USER_GETCREDENTIAL_ERROR("10029","获取临时密钥失败"),

    USER_QQLOGIN_GETOPENID_ERROR("10030","QQ登录获取openId失败"),
    USER_QQLOGIN_GETUSERINFO_ERROR("10031","QQ登录获取用户信息失败"),
    USER_QQLOGIN_GETTOKEN_ERROR("10032","QQ登录获取token失败"),

    SMS_NOT_EXISTS("20000","验证码不存在"),
    SMS_IS_USED("20001","验证码已使用"),
    SMS_IS_EXPIRE("20002","验证码已过期"),

    UNIFIELDORDER_ERROR("30001","微信支付统一下单接口调用失败"),
    VALID_SIGN_ERROR("30002","微信支付支付回调校验签名失败"),
    TOTAL_FEE_ERROR("30003","totalFee参数必须大于0"),

    ORDER_NOT_EXISTS("40001","充值订单记录不存在"),
    ORDER_IS_INVALID("40002","订单记录已失效"),
    ORDER_DATE_ERROR("40003","日期格式有误"),
    RECHARGE_CONF_NOT_EXISTS("40004","充值配置不存在"),


    REDPACKET_IS_OVER("50001","红包已经抢完"),
    REDPACKET_IS_GRABED("50002","亲,您已经抢过,请点其他红包,谢谢"),
    REDPACKET_IS_NOT_EXISTS("50003","红包不存在"),
    REDPACKET_IS_NOT_SELF("50004","自己不能给自己发"),
    REDPACKET_IS_NOT_YOURS("50005","没有权限抢该红包"),

    GIFT_NOT_EXISTS("60001","礼物不存在"),

    WITHDRAWAL_DAY_LIMIT("70001","提现超出每日金额限额"),
    WITHDRAWAL_MONTH_LIMIT("70002","提现超出每月次数限额"),
    WITHDRAWAL_MIN_LIMIT("70003","提现金额小于最低领赏金额"),
    WITHDRAWAL_OPERATION_LIMIT("70004","客服小姐姐很忙,请等她处理完再提交"),

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
    ROOM_JOIN_TENCENT_ERROR("80023","同步腾讯添加用户失败")


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
