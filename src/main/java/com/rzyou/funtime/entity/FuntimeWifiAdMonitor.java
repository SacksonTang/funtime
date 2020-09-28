package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeWifiAdMonitor implements Serializable {

    private static final long serialVersionUID = -7541446089269098649L;
    private Long id;

    private String aid;

    private String cid;

    private String cgid;

    private String uid;

    private String pid;

    private String sid;

    private String stime;

    private Integer os;

    private String idfa;

    private String mac;

    private String imei;

    private String androidId;

    private String ip;

}