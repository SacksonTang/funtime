package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeDeviceInfo implements Serializable {
    private static final long serialVersionUID = -6471971129071138376L;


    private Long id;

    private Long userId;

    private String channel;

    private String point;

    private String imei;

    private String idfa;

    private String androidId;

    private String oaid;

    private String os;

    private String mac;

    private String mac1;

    private String ip;

    private String ua;

    private String brand;

    private String phoneModel;

    private String phoneVersion;

    private String idfv;
}
