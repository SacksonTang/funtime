package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class FuntimeUserNation {
    private Long id;

    private Long userId;

    private Integer nationProvinceId;

    private Integer nationCityId;

    private Integer nationDistrictId;

    private Integer isDefault;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

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

    public Integer getNationProvinceId() {
        return nationProvinceId;
    }

    public void setNationProvinceId(Integer nationProvinceId) {
        this.nationProvinceId = nationProvinceId;
    }

    public Integer getNationCityId() {
        return nationCityId;
    }

    public void setNationCityId(Integer nationCityId) {
        this.nationCityId = nationCityId;
    }

    public Integer getNationDistrictId() {
        return nationDistrictId;
    }

    public void setNationDistrictId(Integer nationDistrictId) {
        this.nationDistrictId = nationDistrictId;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}