package com.rzyou.funtime.common;

public enum OrderState {
    START(1,"待接单"),RECEIVED(2,"已接单"),REFUSED(3,"已拒绝"),SERVICEOVER(4,"服务完成"),COMPLETE(5,"订单完成")
            ,REFUND(6,"退款"),CANCEL(7,"已取消");
    private Integer value;
    private String desc;

    OrderState(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
