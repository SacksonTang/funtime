package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeToutiaoAd implements Serializable {
    private static final long serialVersionUID = -1851819282091012473L;

    private Long id;

    private String aid;

    private String aidName;

    private String advertiserId;

    private String cid;

    private String cidName;

    private String campaignId;

    private String campaignName;

    private String ctype;

    private String csite;

    private String convertId;

    private String requestId;

    private String sl;

    private String imei;

    private String idfa;

    private String androidId;

    private String oaid;

    private String oaidMd5;

    private String os;

    private String mac;

    private String mac1;

    private String ip;

    private String ua;

    private String geo;

    private String ts;

    private String callbackParam;

    private String callbackUrl;

}