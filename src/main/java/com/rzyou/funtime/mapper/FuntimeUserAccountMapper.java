package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface FuntimeUserAccountMapper {

    int updateUserAccountForPlus(@Param("id") Long id, @Param("blackDiamond") BigDecimal blackDiamond, @Param("blueDiamond") BigDecimal blueDiamond, @Param("hornNumber") Integer hornNumber);

    int updateUserAccountForSub(@Param("id") Long id, @Param("blackDiamond") BigDecimal blackDiamond
            , @Param("blueDiamond") BigDecimal blueDiamond, @Param("hornNumber") Integer hornNumber
            , @Param("version") Long version,@Param("newVersion") Long newVersion);


    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccount record);

    FuntimeUserAccount selectByUserId(Long userId);

    int updateByPrimaryKeySelective(FuntimeUserAccount record);

}