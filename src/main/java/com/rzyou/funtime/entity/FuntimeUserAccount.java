package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

public class FuntimeUserAccount {
    private Long id;

    private Long userId;

    private BigDecimal blackDiamond;

    private BigDecimal blueDiamond;

    private Integer hornNumber;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBlackDiamond() {
        return blackDiamond;
    }

    public void setBlackDiamond(BigDecimal blackDiamond) {
        this.blackDiamond = blackDiamond;
    }

    public BigDecimal getBlueDiamond() {
        return blueDiamond;
    }

    public void setBlueDiamond(BigDecimal blueDiamond) {
        this.blueDiamond = blueDiamond;
    }

    public Integer getHornNumber() {
        return hornNumber;
    }

    public void setHornNumber(Integer hornNumber) {
        this.hornNumber = hornNumber;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}