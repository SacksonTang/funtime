package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserRedpacketDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeUserRedpacketDetailMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserRedpacketDetail record);

    FuntimeUserRedpacketDetail selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserRedpacketDetail record);

    int insertBatch(@Param("details") List<FuntimeUserRedpacketDetail> details);

    int updateDetailById(@Param("id") Long id,@Param("grabUserId") Long grabUserId,@Param("version") Long version,@Param("newVersion") Long newVersion);

    List<FuntimeUserRedpacketDetail> queryDetailByRedId(Long redId);

    List<FuntimeUserRedpacketDetail> queryDetailByRedIdAll(Long redId);


    FuntimeUserRedpacketDetail queryDetailByRedIdAndUserId(@Param("redId") Long redId,@Param("grabUserId") Long grabUserId);
}