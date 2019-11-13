package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountRedpacketRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserAccountRedpacketRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccountRedpacketRecord record);

    FuntimeUserAccountRedpacketRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountRedpacketRecord record);

}