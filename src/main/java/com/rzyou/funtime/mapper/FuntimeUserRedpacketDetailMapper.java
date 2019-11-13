package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserRedpacketDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserRedpacketDetailMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FuntimeUserRedpacketDetail record);

    int insertSelective(FuntimeUserRedpacketDetail record);

    FuntimeUserRedpacketDetail selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserRedpacketDetail record);

    int updateByPrimaryKey(FuntimeUserRedpacketDetail record);
}