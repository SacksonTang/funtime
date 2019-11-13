package com.rzyou.funtime.common;

public enum ErrorMsgEnum {

    SUCCESS("0","成功"),
    PARAMETER_ERROR("99999","获取参数失败"),
    UNKNOWN_ERROR("10000","系统繁忙，请稍后再试...."),
    DATA_ORER_ERROR("10001","数据操作失败"),
    USER_NOT_EXISTS("10002","用户不存在"),
    USER_IS_DELETE("10003","用户已封禁"),
    USER_IS_NORMAL("10004","用户已经正常"),
    USER_LOGIN_ERROR("10005","用户登录失败"),
    USER_TOKEN_ERROR("10006","用户令牌token无效"),
    USER_TOKEN_EMPTY("10007","用户令牌token为空"),
    USER_TOKEN_EXPIRE("10008","用户令牌token过期"),
    USER_ID_NOT_EXIST("10009","USERID为空"),
    USER_GETTOKEN_FAIL("10010","获取token失败"),
    USER_SMS_FAIL("10011","短信发送失败"),
    USER_LOGINTYPE_ERROR("10012","登录类型LOGINTYPE错误"),


    SMS_NOT_EXISTS("20000","验证码不存在"),
    SMS_IS_USED("20001","验证码已使用"),
    SMS_IS_EXPIRE("20002","验证码已过期"),

    UNIFIELDORDER_ERROR("30001","微信支付统一下单接口调用失败"),
    VALID_SIGN_ERROR("30002","微信支付支付回调校验签名失败"),
    TOTAL_FEE_ERROR("30003","totalFee参数必须大于0"),

    ORDER_NOT_EXISTS("40001","充值订单记录不存在"),
    ORDER_IS_INVALID("40002","订单记录已失效"),
    RECHARGE_CONF_NOT_EXISTS("40003","充值配置不存在")

    ;

    private String value;
    private String desc;

    private ErrorMsgEnum(String value, String desc) {
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
