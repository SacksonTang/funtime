package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class FuntimeUserAccountCharmRecord implements Serializable {
    private Long id;
    private Long userId;
    private Integer charmVal;
    private Long relationId;
    private Integer type;
}