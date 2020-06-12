package com.rzyou.funtime.mapper;


import com.rzyou.funtime.entity.FuntimeBox;
import com.rzyou.funtime.entity.FuntimeBoxConf;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeBoxMapper {

    FuntimeBox getBoxInfoByBoxNumber(Integer boxNumber);

    List<FuntimeBoxConf> getBoxConfByBoxNumber(Integer boxNumber);

    List<Map<String,Object>> getBoxList();

}
