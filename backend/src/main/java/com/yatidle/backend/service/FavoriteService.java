package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yatidle.backend.entity.Favorite;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.ItemImage;
import com.yatidle.backend.mapper.FavoriteMapper;
import com.yatidle.backend.mapper.ItemImageMapper;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.vo.favorite.FavoriteVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final ItemMapper itemMapper;
    private final ItemImageMapper itemImageMapper;
    private final String baseUrl;

    public FavoriteService(FavoriteMapper favoriteMapper,
                           ItemMapper itemMapper,
                           ItemImageMapper itemImageMapper,
                           @Value("${app.base-url}") String baseUrl) {
        this.favoriteMapper = favoriteMapper;
        this.itemMapper = itemMapper;
        this.itemImageMapper = itemImageMapper;
        this.baseUrl = baseUrl;
    }

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

    public List<FavoriteVO> listMyFavorites(Long currentUserId){
        List<Favorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, currentUserId)
                        .orderByDesc(Favorite::getCreateTime)
        );

        List<FavoriteVO> result = new ArrayList<>();

        for (Favorite favorite : favorites) {
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

            List<ItemImage> images = itemImageMapper.selectByItemId(item.getId());
            if (images != null && !images.isEmpty()) {
                vo.setCoverImage(resolveUrl(images.get(0).getImageUrl()));
            }

            result.add(vo);
        }
        return result;
    }

    private String resolveUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        return baseUrl + url;
    }
}
