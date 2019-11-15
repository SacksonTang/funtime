package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountGifttransRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeUserAccountGifttransRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccountGifttransRecord record);

    FuntimeUserAccountGifttransRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountGifttransRecord record);

    List<FuntimeUserAccountGifttransRecord> getGiftOfSendForPage(@Param("startDate") String startDate
            , @Param("endDate") String endDate,@Param("userId") Long userId);

    List<FuntimeUserAccountGifttransRecord> getGiftOfRecieveForPage(@Param("startDate") String startDate
            , @Param("endDate") String endDate,@Param("userId") Long userId);
}