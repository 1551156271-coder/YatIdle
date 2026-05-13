package com.yatidle.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yatidle.backend.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper

public interface FavoriteMapper extends BaseMapper<Favorite> {
}
