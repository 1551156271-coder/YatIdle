package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.Favorite;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.ItemImage;
import com.yatidle.backend.entity.Wanted;
import com.yatidle.backend.entity.WantedImage;
import com.yatidle.backend.mapper.FavoriteMapper;
import com.yatidle.backend.mapper.ItemImageMapper;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.WantedImageMapper;
import com.yatidle.backend.mapper.WantedMapper;
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
    private final WantedMapper wantedMapper;
    private final WantedImageMapper wantedImageMapper;
    private final String baseUrl;

    public FavoriteService(FavoriteMapper favoriteMapper,
                           ItemMapper itemMapper,
                           ItemImageMapper itemImageMapper,
                           WantedMapper wantedMapper,
                           WantedImageMapper wantedImageMapper,
                           @Value("${app.base-url}") String baseUrl) {
        this.favoriteMapper = favoriteMapper;
        this.itemMapper = itemMapper;
        this.itemImageMapper = itemImageMapper;
        this.wantedMapper = wantedMapper;
        this.wantedImageMapper = wantedImageMapper;
        this.baseUrl = baseUrl;
    }

    public void addFavorite(Long itemId, Long currentUserId){
        if(itemId == null){
            throw new BusinessException("商品ID不能为空");
        }
        Item item = itemMapper.selectById(itemId);

        if (item == null || (item.getIsDeleted() != null && item.getIsDeleted() == 1)) {
            throw new BusinessException("商品不存在");
        }

        Favorite exist = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, currentUserId)
                        .eq(Favorite::getItemId, itemId)
        );

        if (exist != null) {
            throw new BusinessException("已收藏该商品");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(currentUserId);
        favorite.setItemId(itemId);

        favoriteMapper.insert(favorite);
    }

    public void cancelFavorite(Long itemId, Long currentUserId){
        if(itemId == null){
            throw new BusinessException("商品ID不能为空");
        }

        favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, currentUserId)
                        .eq(Favorite::getItemId, itemId)
        );
    }

    public void addWantedFavorite(Long wantedId, Long currentUserId){
        if(wantedId == null){
            throw new BusinessException("求购ID不能为空");
        }
        Wanted wanted = wantedMapper.selectById(wantedId);
        if (wanted == null || (wanted.getIsDeleted() != null && wanted.getIsDeleted() == 1)) {
            throw new BusinessException("求购信息不存在");
        }

        Favorite exist = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, currentUserId)
                        .eq(Favorite::getWantedId, wantedId)
        );
        if (exist != null) {
            throw new BusinessException("已收藏该求购");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(currentUserId);
        favorite.setWantedId(wantedId);
        favoriteMapper.insert(favorite);
    }

    public void cancelWantedFavorite(Long wantedId, Long currentUserId){
        if(wantedId == null){
            throw new BusinessException("求购ID不能为空");
        }
        favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, currentUserId)
                        .eq(Favorite::getWantedId, wantedId)
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
            if (favorite.getItemId() != null) {
                Item item = itemMapper.selectById(favorite.getItemId());
                if(item == null || (item.getIsDeleted() != null && item.getIsDeleted() == 1)){
                    continue;
                }
                FavoriteVO vo = new FavoriteVO();
                vo.setId(favorite.getId());
                vo.setType("item");
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
            } else if (favorite.getWantedId() != null) {
                Wanted wanted = wantedMapper.selectById(favorite.getWantedId());
                if(wanted == null || (wanted.getIsDeleted() != null && wanted.getIsDeleted() == 1)){
                    continue;
                }
                FavoriteVO vo = new FavoriteVO();
                vo.setId(favorite.getId());
                vo.setType("wanted");
                vo.setWantedId(wanted.getId());
                vo.setWantedTitle(wanted.getTitle());
                vo.setWantedStatus(wanted.getStatus());
                vo.setBudgetMin(wanted.getBudgetMin());
                vo.setBudgetMax(wanted.getBudgetMax());
                vo.setCreateTime(favorite.getCreateTime());

                List<WantedImage> images = wantedImageMapper.selectByWantedId(wanted.getId());
                if (images != null && !images.isEmpty()) {
                    vo.setCoverImage(resolveUrl(images.get(0).getImageUrl()));
                }
                result.add(vo);
            }
        }
        return result;
    }

    private String resolveUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        return baseUrl + url;
    }
}
