package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserFileMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FuntimeUserFile record);

    int insertSelective(FuntimeUserFile record);

    FuntimeUserFile selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserFile record);

    int updateByPrimaryKey(FuntimeUserFile record);
}