package com.rzyou.funtime.common;

public class ResultMsg <T>{

    String code;
    String token;
    String msg;
    T data;


    public ResultMsg() {
        this.code = "0";
        this.msg = "操作成功";
    }

    public ResultMsg(String token) {
        this.code = "0";
        this.msg = "操作成功";
        this.token = token;
    }

    public ResultMsg(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultMsg(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResultMsg(T data) {
        this.code = "200";
        this.msg = "操作成功";
        this.data = data;
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
