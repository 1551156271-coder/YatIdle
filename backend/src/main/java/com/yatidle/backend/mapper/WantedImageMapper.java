package com.yatidle.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yatidle.backend.entity.WantedImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WantedImageMapper extends BaseMapper<WantedImage> {

    @Select("SELECT * FROM wanted_image WHERE wanted_id = #{wantedId} ORDER BY sort_order ASC")
    List<WantedImage> selectByWantedId(@Param("wantedId") Long wantedId);

    @Delete("DELETE FROM wanted_image WHERE wanted_id = #{wantedId}")
    int deleteByWantedId(@Param("wantedId") Long wantedId);

    @Delete("DELETE FROM wanted_image WHERE wanted_id IN (SELECT id FROM wanted WHERE user_id = #{userId})")
    int deleteByUserId(@Param("userId") Long userId);
}
