package com.yatidle.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yatidle.backend.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {
    // 关键词搜索（LIKE 模糊查询）
    @Select("SELECT * FROM item WHERE is_deleted = 0 AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))")
    List<Item> searchByKeyword(String keyword);

    // 增加浏览次数（线程安全，用数据库自增）
    @Update("UPDATE item SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);

    // 增加收藏次数
    @Update("UPDATE item SET favorite_count = favorite_count + 1 WHERE id = #{id}")
    int incrementFavoriteCount(Long id);

    // 减少收藏次数
    @Update("UPDATE item SET favorite_count = favorite_count - 1 WHERE id = #{id} AND favorite_count > 0")
    int decrementFavoriteCount(Long id);
}
