package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.music.FuntimeMusicTag;
import com.rzyou.funtime.entity.music.FuntimeUserMusic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeMusicMapper {

    int insertMusic(Map<String,Object> map);

    List<String> getLocalMusics2();

    List<Map<String,Object>> getLocalMusics( @Param("tagId") Integer tagId,@Param("content")  String content);

    int updateMusicHot(Integer musicId);

    List<Map<String,Object>>  getMusicList(@Param("content") String content,@Param("userId")  Long userId);

    List<Map<String, Object>> getMyMusics(@Param("content") String content, @Param("userId") Long userId, @Param("musicTagIds") String musicTagIds);

    Long getUserMusicById(Long id);

    Long getUserMusic(@Param("userId") Long userId,@Param("musicId") Integer musicId);

    Integer getMusicTagByName(@Param("tagName") String tagName, @Param("userId") Long userId);

    List<Map<String,Object>> getMusicTagByUser(Long userId);

    List<Map<String,Object>> getUserMusicTag(@Param("userMusicId") Long userMusicId,@Param("userId") Long userId);

    Integer getUserMusicTagCount(Long userMusicId);

    Integer getUserMusicTagCount2(Long musicTagId);

    int insertUserMusic(FuntimeUserMusic userMusic);

    int deleteUserMusic(Long id);

    int insertMusicTag(FuntimeMusicTag musicTag);

    int updateMusicTagById(FuntimeMusicTag musicTag);

    int deleteMusicTag(Long id);

    int insertUserMusicTag(@Param("userMusicId") Long userMusicId,@Param("musicTagId") Long musicTagId);

    int deleteUserMusicTag(Long userMusicId);

    int deleteUserMusicTag2(Long musicTagId);



}