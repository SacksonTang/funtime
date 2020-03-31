package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface FuntimeUserAccountMapper {

    int updateUserAccountGoldCoinPlus(@Param("id") Long id,@Param("goldCoin") Integer goldCoin);

    int updateUserAccountGoldCoinSub(@Param("id") Long id,@Param("goldCoin") Integer goldCoin);

    int updateUserAccountForPlus(@Param("id") Long id, @Param("blackDiamond") BigDecimal blackDiamond, @Param("blueDiamond") BigDecimal blueDiamond, @Param("hornNumber") Integer hornNumber);

    int updateUserAccountForPlusGift(@Param("id") Long id, @Param("blackDiamond") BigDecimal blackDiamond, @Param("receivedGiftNum") Integer receivedGiftNum,@Param("charmVal") Integer charmVal);

    int updateUserAccountForSub(@Param("id") Long id, @Param("blackDiamond") BigDecimal blackDiamond
            , @Param("blueDiamond") BigDecimal blueDiamond, @Param("hornNumber") Integer hornNumber
            , @Param("version") Long version,@Param("newVersion") Long newVersion);

    int updateUserAccountLevel(@Param("id") Long id, @Param("level") Integer level, @Param("blueDiamond") BigDecimal blueDiamond, @Param("hornNumber") Integer hornNumber, @Param("levelVal") Integer levelVal, @Param("wealthVal") Integer wealthVal);

    int updateUserAccountForConvert(@Param("id") Long id, @Param("level") Integer level, @Param("blueDiamond") BigDecimal blueDiamond, @Param("blackDiamond") BigDecimal blackDiamond, @Param("levelVal") Integer levelVal, @Param("wealthVal") Integer wealthVal);

    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccount record);

    FuntimeUserAccount selectByUserId(Long userId);

    int updateByPrimaryKeySelective(FuntimeUserAccount record);

}