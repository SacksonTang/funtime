package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class FuntimeUserPhotoAlbum implements Serializable {
    private static final long serialVersionUID = -7054849645739660860L;
    private Long id;

    private Long userId;

    private String resourceUrl;

    private String resourceKeyUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private Integer flag;

    private Integer sort;


}