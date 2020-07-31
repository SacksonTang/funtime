package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class FuntimeTencentAdMonitor implements Serializable {

    private static final long serialVersionUID = -864110163998249934L;
    private String clickId;

    private String muid;

    private String clickTime;

    private String ip;

    private String userAgent;

    private Integer campaignId;

    private Integer adgroupId;

    private Integer adId;

    private Integer adPlatformType;

    private Integer accountId;

    private Integer agencyId;

    private Integer billingEvent;

    private Integer promotedObjectType;
    private Integer realCost;

    private String deeplinkUrl;

    private String universalLink;

    private String pageUrl;

    private String deviceOsType;

    private String clickSkuId;

    private Integer processTime;

    private String promotedObjectId;

    private String impressionId;

    private String requestId;

    private String hashAndroidId;

    private String hashMac;

    private String callback;

    private Integer siteSet;

    private Integer encryptedPositionId;

    private String ipv6;

    private String oaid;

    private String hashOaid;


}