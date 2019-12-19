package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
@Data
public class FuntimeAccusation {
    private Long id;

    private Integer type;

    private Integer typeTagId;

    private Long userId;

    private Long accusationId;

    private String img1;

    private String img2;

    private String img3;

    private String img4;

    private String img5;

    private Integer state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updateTime;


}