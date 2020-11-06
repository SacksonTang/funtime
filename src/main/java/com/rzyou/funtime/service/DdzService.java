package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeDdz;

import java.util.List;
import java.util.Map;

public interface DdzService {
    void goldChange(FuntimeDdz ddz);

    List<Map<String,Object>> getRankList();
}
