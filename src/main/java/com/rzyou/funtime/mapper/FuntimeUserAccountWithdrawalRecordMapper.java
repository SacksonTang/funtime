package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountWithdrawalRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserAccountWithdrawalRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccountWithdrawalRecord record);

    FuntimeUserAccountWithdrawalRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountWithdrawalRecord record);

}