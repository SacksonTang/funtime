package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeNoticeMapper {

    int insertSelective(FuntimeNotice record);

    List<FuntimeNotice> getGroupFailNotice();

    List<FuntimeNotice> getSingleFailNotice();

    List<FuntimeNotice> getAllRoomFailNotice();

    List<FuntimeNotice> getAllFailNotice();

    List<FuntimeNotice> getSingleFailNoticeNoRoom();

    int updateState(@Param("id") Long id, @Param("state") Integer state);


}