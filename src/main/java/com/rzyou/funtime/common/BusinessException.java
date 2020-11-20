package com.rzyou.funtime.common;

public class BusinessException extends RuntimeException {

    private String code;
    private String msg;

    public BusinessException(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public BusinessException(ErrorMsgEnum errorMsgEnum) {
        this.code = errorMsgEnum.getValue();
        this.msg = errorMsgEnum.getDesc();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
