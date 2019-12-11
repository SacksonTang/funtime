package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserPhotoAlbum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface FuntimeUserPhotoAlbumMapper {

    List<FuntimeUserPhotoAlbum> getPhotoAlbumByUserId(Long userId);

    int deleteByUserId(Long userId);

    int insertBatch(@Param("photos") List<FuntimeUserPhotoAlbum> record);

    FuntimeUserPhotoAlbum selectByPrimaryKey(Long id);


}