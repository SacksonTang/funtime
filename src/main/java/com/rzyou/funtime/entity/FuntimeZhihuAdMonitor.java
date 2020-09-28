package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeZhihuAdMonitor implements Serializable {

    private static final long serialVersionUID = -283582954849697115L;
    private Long id;

    private String imei;

    private String oaid;

    private String androidId;

    private String idfa;

    private String sessionId;

    private String callback;

    private String ip;

    private String ua;

    private Integer os;

    private String campaign;

    private String creative;

    private Integer ts;

}