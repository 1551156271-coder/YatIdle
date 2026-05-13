package com.yatidle.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yatidle.backend.entity.ItemImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ItemImageMapper extends BaseMapper<ItemImage> {
    @Select("SELECT * FROM item_image WHERE item_id = #{itemId} AND is_deleted = 0 ORDER BY sort_order ASC")
    List<ItemImage> selectByItemId(Long itemId);

    @Delete("DELETE FROM item_image WHERE item_id = #{itemId}")
    int deleteByItemId(Long itemId);
}
