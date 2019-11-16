package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAgreement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FuntimeUserAgreementMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAgreement record);

    FuntimeUserAgreement selectByUserId(@Param("userId") Long userId,@Param("type") Integer type);

    int updateByPrimaryKeySelective(FuntimeUserAgreement record);

}