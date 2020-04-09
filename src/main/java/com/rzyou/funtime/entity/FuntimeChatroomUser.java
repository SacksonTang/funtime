package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.web.util.pattern.PathPattern;

import java.io.Serializable;
import java.util.Date;
@Data
public class FuntimeChatroomUser implements Serializable {
    private static final long serialVersionUID = -4072409694383772070L;
    private Long id;

    private Long roomId;

    private Long userId;

    private Integer userRole;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

}