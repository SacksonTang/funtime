package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserRedpacket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeUserRedpacketMapper {

    int updateStateForInvalid();

    int updateStateById(@Param("state") Integer state,@Param("id") Long id);

    List<FuntimeUserRedpacket> getRedpacketInfoByUserId(@Param("startDate") String startDate
            ,@Param("endDate") String endDate,@Param("userId") Long userId);

    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserRedpacket record);

    FuntimeUserRedpacket selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserRedpacket record);

    int updateByPrimaryKey(FuntimeUserRedpacket record);
}