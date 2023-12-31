package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountCharmRecord;
import com.rzyou.funtime.entity.FuntimeUserAccountGifttransRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeUserAccountGifttransRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccountGifttransRecord record);

    int insertCharmRecord(FuntimeUserAccountCharmRecord record);

    FuntimeUserAccountGifttransRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountGifttransRecord record);

    List<FuntimeUserAccountGifttransRecord> getGiftOfSendForPage(@Param("startDate") String startDate
            , @Param("endDate") String endDate,@Param("userId") Long userId);

    List<FuntimeUserAccountGifttransRecord> getGiftOfRecieveForPage(@Param("startDate") String startDate
            , @Param("endDate") String endDate,@Param("userId") Long userId);

    List<Map<String,Object>> getGiftsByUserId(Long userId);
}