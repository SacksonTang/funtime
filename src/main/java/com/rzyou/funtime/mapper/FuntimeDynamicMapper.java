package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeComment;
import com.rzyou.funtime.entity.FuntimeDynamic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeDynamicMapper {

    int insertDynamic(FuntimeDynamic dynamic);

    int insertComment(FuntimeComment comment);

    int delDynamic(Long id);

    int delCommentByDynamicId(Long dynamicId);

    int delCommentById(Long id);

    Long getDynamicById(Long id);

    Long getCommentById(Long id);

    int insertDynamicLike(@Param("userId") Long userId, @Param("dynamicId") Long dynamicId);

    Integer checkDynamicLike(@Param("userId") Long userId, @Param("dynamicId") Long dynamicId);

    int delDynamicLike(@Param("userId") Long userId, @Param("dynamicId") Long dynamicId);

    Map<String,Object> getDynamicDetailById(@Param("userId") Long userId, @Param("dynamicId") Long dynamicId);

    List<Map<String,Object>> getDynamicList(@Param("counts") Integer counts, @Param("lastId") Long lastId, @Param("userId") Long userId);

    List<Map<String,Object>> getMyDynamicList(@Param("counts") Integer counts, @Param("lastId") Long lastId, @Param("userId") Long userId);

    List<Map<String,Object>> getOtherDynamicList(@Param("counts") Integer counts, @Param("lastId") Long lastId, @Param("userId") Long userId, @Param("toUserId") Long toUserId);

    List<Map<String,Object>> getCommentList(@Param("counts") Integer counts, @Param("lastId") Long lastId,@Param("dynamicId") Long dynamicId);

    List<Map<String, Object>> getLikeList(@Param("counts") Integer counts, @Param("lastId") Long lastId, @Param("dynamicId") Long dynamicId);
}
