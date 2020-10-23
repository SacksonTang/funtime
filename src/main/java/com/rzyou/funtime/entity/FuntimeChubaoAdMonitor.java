package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class FuntimeChubaoAdMonitor implements Serializable {

    private static final long serialVersionUID = -787719894526042629L;
    private Long id;

    private String crid;

    private String cid;

    private String ip;

    private String oaid;

    private String osv;

    private String androidid;

    private String imei;

    private String mac;

    private String idfa;

    private String make;

    private String model;

    private String os;

    private String pkg;

    private String clickid;

    private String callbackUrl;

}