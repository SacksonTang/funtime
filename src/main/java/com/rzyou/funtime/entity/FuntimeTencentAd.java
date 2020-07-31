package com.rzyou.funtime.entity;


import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeTencentAd implements Serializable {

    private static final long serialVersionUID = 3920890903881007175L;
    private String clickId;
    private String muid;
    private String clickTime;
    private Integer appid;
    private Integer advertiserId;
    private String appType;
    private String androidId;
    private String mac;
    private String ip;
    private String userAgent;
    private Integer campaignId;
    private Integer adgroupId;
    private Integer creativeId;

    private Integer agentId;

    private String deeplinkUrl;

    private String destUrl;

    private String deviceOsType;

    private Integer clickSkuId;

    private Integer processTime;

    private Integer productType;

    private String requestId;

    private Integer siteSet;

    private String adgroupName;
    private String oaid;


}