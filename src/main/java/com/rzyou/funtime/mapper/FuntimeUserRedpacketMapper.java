package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserRedpacket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserRedpacketMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FuntimeUserRedpacket record);

    int insertSelective(FuntimeUserRedpacket record);

    FuntimeUserRedpacket selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserRedpacket record);

    int updateByPrimaryKey(FuntimeUserRedpacket record);
}