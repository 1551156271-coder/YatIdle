package com.yatidle.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yatidle.backend.entity.Wanted;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WantedMapper extends BaseMapper<Wanted> {

    @Delete("DELETE FROM wanted WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
