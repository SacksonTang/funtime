package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
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

    private Integer birthday;

    private Integer height;

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

    private Long roomId;

    private Integer guildId;

    private Integer platform;

    private Integer channel;

    private Long showId;

    private Integer age;

    private Integer isLock;

    private Integer isBlock;

    private String constellation;



}