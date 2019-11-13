package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountGifttransRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserAccountGifttransRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccountGifttransRecord record);

    FuntimeUserAccountGifttransRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountGifttransRecord record);

}