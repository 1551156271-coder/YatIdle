package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yatidle.backend.entity.Favorite;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.mapper.FavoriteMapper;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.vo.PageVO;
import com.yatidle.backend.vo.favorite.FavoriteVO;
import jakarta.servlet.http.PushBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final ItemMapper itemMapper;

    public void addFavorite(Long itemId, Long currentUserId){
        if(itemId == null){
            throw new RuntimeException("商品ID不能为空");
        }
        Item item = itemMapper.selectById(itemId);

        if (item == null || (item.getIsDeleted() != null && item.getIsDeleted() == 1)) {
            throw new RuntimeException("商品不存在");
        }

        Favorite exist = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, currentUserId)
                        .eq(Favorite::getItemId, itemId)
        );

        if (exist != null) {
            throw new RuntimeException("已收藏该商品");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(currentUserId);
        favorite.setItemId(itemId);

        favoriteMapper.insert(favorite);
    }

    public void cancelFavorite(Long itemId, Long currentUserId){
        if(itemId == null){
            throw new RuntimeException("商品ID不能为空");
        }

        favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, currentUserId)
                        .eq(Favorite::getItemId, itemId)
        );
    }

    public PageVO<FavoriteVO> listMyFavorites(Long currentUserId, Long pageNum, Long pageSize){
        Page<Favorite> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, currentUserId)
                .orderByDesc(Favorite::getCreateTime);

        Page<Favorite> resultPage = favoriteMapper.selectPage(page, wrapper);

        List<FavoriteVO> result = new ArrayList<>();

        for (Favorite favorite : resultPage.getRecords()) {
            Item item = itemMapper.selectById(favorite.getItemId());

            if(item == null || (item.getIsDeleted() != null && item.getIsDeleted() == 1)){
                continue;
            }

            FavoriteVO vo = new FavoriteVO();
            vo.setId(favorite.getId());
            vo.setItemId(item.getId());
            vo.setItemTitle(item.getTitle());
            vo.setPrice(item.getPrice());
            vo.setItemStatus(item.getStatus());
            vo.setCreateTime(favorite.getCreateTime());

            result.add(vo);
        }

        PageVO<FavoriteVO> vo = new PageVO<>();
        vo.setTotal(page.getTotal());
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        vo.setRecords(result);

        return vo;
    }
}
