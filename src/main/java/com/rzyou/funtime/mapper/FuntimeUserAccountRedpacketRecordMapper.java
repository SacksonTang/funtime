package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountRedpacketRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeUserAccountRedpacketRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccountRedpacketRecord record);

    FuntimeUserAccountRedpacketRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountRedpacketRecord record);

    List<FuntimeUserAccountRedpacketRecord> getRedpacketRecordByredId(Long redId);

    int updateTagById(@Param("id") Long id,@Param("tagId") Integer tagId);


    List<FuntimeUserAccountRedpacketRecord> getRedpacketOfRecieveForPage(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("userId") Long userId);
}