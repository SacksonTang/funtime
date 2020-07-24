package com.rzyou.funtime.entity.music;

import lombok.Data;

import java.io.Serializable;

@Data
public class FuntimeUserMusic implements Serializable {
    private static final long serialVersionUID = -2886078952526876030L;
    private Long id;
    private Long userId;
    private Integer musicId;
}
