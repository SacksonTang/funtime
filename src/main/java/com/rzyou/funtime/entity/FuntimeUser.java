package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class FuntimeUser implements Serializable {
    private static final long serialVersionUID = 8830346720854513695L;
    private Long id;

    private String username;

    private String fullname;

    private String nickname;

    private String password;

    private String phoneNumber;

    private String phoneImei;

    private Integer drainageChannelId;

    private String portraitAddress;

    private Integer sex;

    private String sexColor;

    private Integer birthday;

    private Integer height;

    private String heightColor;

    private String signText;

    private Integer state;

    private Integer realnameAuthenticationFlag;

    private String token;

    private Integer onlineState;

    private Integer createRoom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date registrationTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date lastLoginTime;

    private Integer concerns;

    private Integer fans;

    private String ip;

    private Integer flag;

    private Long version;

    private Long newVersion;

    private String code;

    private String loginType;

    private String deviceName;

    private String province;

    private String city;

    private String district;

    private String longitude;

    private String latitude;

    private String locationDesc;

    private List<Integer> tags;

    private List<Map<String,Object>> tagNames;

    private Long roomId;

    private Integer guildId;

    private Integer platform;

    private Integer channel;

    private Long showId;

    private Integer age;

    private Integer isLock;

    private Integer isBlock;

    private String constellation;

    private Integer blueAmount;

    private Boolean newUser;

    private Integer roomState;

    private String privacyAgreementUrl;

    private String userAgreementUrl;

    private Boolean concerned;

}