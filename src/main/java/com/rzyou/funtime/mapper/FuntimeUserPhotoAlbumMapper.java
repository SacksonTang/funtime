package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserPhotoAlbum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface FuntimeUserPhotoAlbumMapper {

    List<FuntimeUserPhotoAlbum> getPhotoAlbumByUserId(Long userId);

    int deleteByPrimaryKey(Long id);

    int insert(FuntimeUserPhotoAlbum record);

    int insertSelective(FuntimeUserPhotoAlbum record);

    FuntimeUserPhotoAlbum selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserPhotoAlbum record);

    int updateByPrimaryKey(FuntimeUserPhotoAlbum record);
}