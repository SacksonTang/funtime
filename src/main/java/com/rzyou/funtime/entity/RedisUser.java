package com.rzyou.funtime.entity;

import java.io.Serializable;

public class RedisUser implements Serializable {
    private static final long serialVersionUID = -7025226008674266563L;
    public String token;
    public String uuid;
    public int onlineState;
}
