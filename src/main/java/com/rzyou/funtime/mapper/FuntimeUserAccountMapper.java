package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccount;
import com.rzyou.funtime.entity.FuntimeUserAccountFishRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeUserAccountMapper {

    List<Map<String, Object>> getLevelConf();

    int updateUserAccountGoldConvert(@Param("id") Long id,@Param("goldCoin") Integer goldCoin,@Param("blueDiamond") BigDecimal blueDiamond);

    int updateUserAccountGoldCoinPlus(@Param("id") Long id,@Param("goldCoin") Integer goldCoin);

    int updateUserAccountGoldCoinSub(@Param("id") Long id,@Param("goldCoin") Integer goldCoin);

    int updateUserAccountForPlus(@Param("id") Long id, @Param("blackDiamond") BigDecimal blackDiamond, @Param("blueDiamond") BigDecimal blueDiamond, @Param("hornNumber") Integer hornNumber);

    int updateUserAccountForPlusGift(@Param("id") Long id, @Param("blackDiamond") BigDecimal blackDiamond, @Param("receivedGiftNum") Integer receivedGiftNum,@Param("charmVal") Integer charmVal);

    int updateUserAccountForSub(@Param("id") Long id, @Param("blackDiamond") BigDecimal blackDiamond
            , @Param("blueDiamond") BigDecimal blueDiamond, @Param("hornNumber") Integer hornNumber
            , @Param("version") Long version,@Param("newVersion") Long newVersion);

    int updateUserAccountLevel(@Param("id") Long id, @Param("level") Integer level, @Param("blueDiamond") BigDecimal blueDiamond, @Param("hornNumber") Integer hornNumber, @Param("levelVal") Integer levelVal, @Param("wealthVal") Integer wealthVal,@Param("goldNum") Integer goldNum);

    int updateUserAccountForConvert(@Param("id") Long id, @Param("level") Integer level, @Param("blueDiamond") BigDecimal blueDiamond, @Param("blackDiamond") BigDecimal blackDiamond, @Param("levelVal") Integer levelVal, @Param("wealthVal") Integer wealthVal);

    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccount record);

    FuntimeUserAccount selectByUserId(Long userId);

    int updateByPrimaryKeySelective(FuntimeUserAccount record);

    Map<String, Object> getBulletOfFish(Long userId);

    int saveScoreOfFish(@Param("userId") Long userId,@Param("score") Integer score, @Param("bullet")Integer bullet);

    int updateBulletForPlus(@Param("userId") Long userId,@Param("bullet")Integer bullet);

    int insertFishRecord(@Param("userId") Long userId,@Param("score") Integer score, @Param("bullet")Integer bullet);

    int insertFishAccount(@Param("userId") Long userId,@Param("score") Integer score, @Param("bullet")Integer bullet);

    int insertFishAccountRecord(FuntimeUserAccountFishRecord record);

    List<Map<String, Object>> getFishRanklist(int endCount);

    List<Map<String, Object>> getFishRanklist2( @Param("endCount") Integer endCount,@Param("startDate") String startDate,@Param("endDate") String endDate);

    Long checkUserKnapsackExist(@Param("userId") Long userId, @Param("itemId") Integer itemId,@Param("type") int type);

    Integer getItemNumByUserId(@Param("userId") Long userId, @Param("itemId") Integer itemId,@Param("type") int type);

    int updateUserKnapsackPlus(@Param("id") Long id,@Param("num") int num);

    int updateUserKnapsackSub(@Param("id") Long id,@Param("num") int num);

    int insertKnapsackLog(@Param("userId") Long userId, @Param("type") int type,  @Param("itemId") Integer itemId, @Param("itemNum") int itemNum,@Param("actionType") String actionType,@Param("operationType") String operationType);

    int insertUserKnapsack(@Param("userId") Long userId, @Param("type") int type,  @Param("itemId") Integer itemId, @Param("itemNum") int itemNum,@Param("version") Long version);
}