package com.rzyou.funtime.service;

public interface RedisService {
    Object get(String key);

    boolean set(String key,String value);

    boolean set(String key,String value,long time);

}
