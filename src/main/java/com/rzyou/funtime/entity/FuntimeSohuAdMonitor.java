package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeSohuAdMonitor implements Serializable{

    private static final long serialVersionUID = -6412544941512897627L;
    private Long id;

    private String os;

    private String imei;

    private String oaid;

    private String idfa;

    private String idfa1;

    private String gid;

    private String aid;

    private String clickts;

    private String clickid;

    private String callback;

    private String ip;

    private String ua;

}