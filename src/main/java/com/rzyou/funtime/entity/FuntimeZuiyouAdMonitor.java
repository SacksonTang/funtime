package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeZuiyouAdMonitor implements Serializable {

    private static final long serialVersionUID = 1610877053219270937L;
    private Long id;

    private String idfa;

    private String imei;

    private String campaignid;

    private String creativeid;

    private String callback;

    private String os;

    private String androidid;

    private String oaid;

    private String ip;

    private String ua;

    private String ts;

}