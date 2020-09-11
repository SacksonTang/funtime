package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeDdz;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeDdzMapper {

    int insertDdzRecord(FuntimeDdz ddz);

}
