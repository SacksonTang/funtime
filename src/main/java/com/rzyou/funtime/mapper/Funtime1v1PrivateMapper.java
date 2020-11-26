package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.Funtime1v1Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface Funtime1v1PrivateMapper {

    List<Map<String,Object>> get1v1price(Integer times);

    Map<String,Object> get1v1priceById(Integer id);

    List<Funtime1v1Record> get1v1RecordTask();

    Integer get1v1Counts(@Param("userId") Long userId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    int save1V1Record(Funtime1v1Record record);

    int cancelMatch();

    int compeleteMatch(@Param("id") Long id,@Param("roomId") Long roomId);

    int cancelMatchById(Long id);
}
