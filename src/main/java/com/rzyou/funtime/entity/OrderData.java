package com.rzyou.funtime.entity;

import java.io.Serializable;

public class OrderData implements Serializable {

    private static final long serialVersionUID = 7253834507904119189L;
    private String device_info = "";//设备号

    private String body = "";       //商品简单描述  维康动力-医疗###

    private String detail = "";     //商品详情

    private String attach = "";     //附加数据,在查询时,原样返回 待定

    private String out_trade_no = "";//订单号 ***

    private String fee_type = "";   //类型,不传,默认为CNY

    private String total_fee = "";  //总计多少钱 ***

    private String spbill_create_ip = ""; //用户端ip,必传 ***

    private String time_start  = "";//###订单生成时间--->这里指的是预付单生成时间###

    private String time_expire = "";//###订单失效时间--->预付单失效后,如果用户还要提交支付,需要发起重新请求订单接口,获得新的预付单id###

    private String goods_tag = "";  //订单优惠说明 待定

    private String notify_url = ""; //回调地址***

    private String trade_type = ""; //JSAPI ***

    private String product_id = ""; //商品id,###

    private String limit_pay = "";  //非信用卡支付

    private String openid = "";     //用户openid***

    private String scene_info = ""; //场景信息

    public String getDevice_info() {
        return device_info;
    }

    public void setDevice_info(String device_info) {
        this.device_info = device_info;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getFee_type() {
        return fee_type;
    }

    public void setFee_type(String fee_type) {
        this.fee_type = fee_type;
    }

    public String getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }

    public String getSpbill_create_ip() {
        return spbill_create_ip;
    }

    public void setSpbill_create_ip(String spbill_create_ip) {
        this.spbill_create_ip = spbill_create_ip;
    }

    public String getTime_start() {
        return time_start;
    }

    public void setTime_start(String time_start) {
        this.time_start = time_start;
    }

    public String getTime_expire() {
        return time_expire;
    }

    public void setTime_expire(String time_expire) {
        this.time_expire = time_expire;
    }

    public String getGoods_tag() {
        return goods_tag;
    }

    public void setGoods_tag(String goods_tag) {
        this.goods_tag = goods_tag;
    }

    public String getNotify_url() {
        return notify_url;
    }

    public void setNotify_url(String notify_url) {
        this.notify_url = notify_url;
    }

    public String getTrade_type() {
        return trade_type;
    }

    public void setTrade_type(String trade_type) {
        this.trade_type = trade_type;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getLimit_pay() {
        return limit_pay;
    }

    public void setLimit_pay(String limit_pay) {
        this.limit_pay = limit_pay;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getScene_info() {
        return scene_info;
    }

    public void setScene_info(String scene_info) {
        this.scene_info = scene_info;
    }
}
