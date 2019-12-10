package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class FuntimeRechargeConf implements Serializable {
    private static final long serialVersionUID = -507150281942903742L;
    private Integer id;

    private BigDecimal rechargeRmb;

    private BigDecimal rechargeNum;

    private BigDecimal convertibleRatio;

    private Integer tagId;

    private String tagName;

    private Integer hornNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getRechargeRmb() {
        return rechargeRmb;
    }

    public void setRechargeRmb(BigDecimal rechargeRmb) {
        this.rechargeRmb = rechargeRmb;
    }

    public BigDecimal getRechargeNum() {
        return rechargeNum;
    }

    public void setRechargeNum(BigDecimal rechargeNum) {
        this.rechargeNum = rechargeNum;
    }

    public BigDecimal getConvertibleRatio() {
        return convertibleRatio;
    }

    public void setConvertibleRatio(BigDecimal convertibleRatio) {
        this.convertibleRatio = convertibleRatio;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName == null ? null : tagName.trim();
    }

    public Integer getHornNum() {
        return hornNum;
    }

    public void setHornNum(Integer hornNum) {
        this.hornNum = hornNum;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}