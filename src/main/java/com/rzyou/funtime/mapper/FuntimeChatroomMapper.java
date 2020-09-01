package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeChatroom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeChatroomMapper {

    Long getInvitationConf();

    List<FuntimeChatroom> getRoomCloseTask();

    int deleteByPrimaryKey(Long id);

    List<Long> getAllRoom();

    FuntimeChatroom getRoomInfoById(Long id);

    Long checkRoomExists(Long id);

    int deleteByRoomId(Long id);

    int insertSelective(FuntimeChatroom record);

    FuntimeChatroom selectByPrimaryKey(Long id);

    FuntimeChatroom getRoomByUserId(Long userId);

    int updateByPrimaryKeySelective(FuntimeChatroom record);

    int updateChatroomState(@Param("id") Long id, @Param("state") Integer state);

    int updateChatroomBlock(@Param("id") Long id, @Param("isBlock") Integer isBlock);

    int updateChatroomBackgroundId(@Param("id") Long id, @Param("backgroundId") Integer backgroundId);

    int updateOnlineNumPlus(@Param("id") Long id, @Param("hots") int hots);

    int updateOnlineNumSub(@Param("id") Long id, @Param("hots") int hots);

    int updateHotsPlus(@Param("id") Long id, @Param("hots") int hots);

    int updateHotsSub(@Param("id") Long id, @Param("hots") int hots);

    int resetRoomHotsTask();

    int updateOnlineNumTask();

    int updateScreenFlag(@Param("roomId") Long roomId,@Param("flag") Integer flag);

    int updateRankFlag(@Param("roomId") Long roomId,@Param("flag") Integer flag);

    int insertUserRoomLog(Map<String, Object> map);

    List<Map<String, Object>> getRoomList(Integer tagId);

    List<Map<String, Object>> getRoomList2(Integer tagId);

    List<Map<String, Object>> getRoomLogList(Long userId);

    List<Map<String, Object>> getRecommendRoomList1();

    List<Map<String, Object>> getRecommendRoomList2(List<Long> list);

    List<Map<String, Object>> getRoomContributionList(@Param("endCount") Integer endCount, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("roomId") Long roomId,@Param("userId") Long userId);

    List<Map<String, Object>> getRoomCharmList(@Param("endCount") Integer endCount,@Param("startDate") String startDate,@Param("endDate") String endDate,@Param("roomId") Long roomId);

    List<Map<String, Object>> getRoomStatement(@Param("startDate") String startDate, @Param("endDate")String endDate, @Param("roomId") Long roomId, @Param("userId") Long userId);
}