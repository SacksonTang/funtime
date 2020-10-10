package com.rzyou.funtime.entity;


import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeBstationAdMonitor implements Serializable {

    private static final long serialVersionUID = -4737686138598012048L;
    private Long id;

    private Integer channel;

    private String trackid;

    private String creativeid;

    private Integer os;

    private String imei;

    private String mac1;

    private String idfa;

    private String aaid;

    private String androidid;

    private String oaid;

    private String ip;

    private String ua;

    private String ts;

    private String shopid;

    private String upmid;

}