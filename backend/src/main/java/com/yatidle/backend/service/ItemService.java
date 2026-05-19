package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.dto.item.ItemPublishDTO;
import com.yatidle.backend.dto.item.ItemSearchDTO;
import com.yatidle.backend.entity.Category;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.ItemImage;
import com.yatidle.backend.mapper.CategoryMapper;
import com.yatidle.backend.mapper.ItemImageMapper;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.vo.item.ItemCardVO;
import com.yatidle.backend.vo.item.ItemDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    public static final String STATUS_ON_SALE = "ON_SALE";
    public static final String STATUS_SOLD = "SOLD";
    public static final String STATUS_REMOVED = "REMOVED";

    private final ItemMapper itemMapper;
    private final ItemImageMapper itemImageMapper;
    private final CategoryMapper categoryMapper;

    // ======================= 1. 发布商品 =======================
    @Transactional(rollbackFor = Exception.class)
    public ItemDetailVO publish(ItemPublishDTO dto) {
        validateCategory(dto.getCategoryId());

        // DTO → Entity
        Item item = new Item();
        item.setUserId(dto.getUserId());
        item.setTitle(dto.getTitle());
        item.setCampus(dto.getCampus());
        item.setConditionLevel(dto.getConditionLevel());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setCategoryId(dto.getCategoryId());
        item.setStatus(STATUS_ON_SALE);
        item.setViewCount(0);
        item.setFavoriteCount(0);
        item.setIsDeleted(0);
        // createTime / updateTime 由 MyMetaObjectHandler 自动填充

        itemMapper.insert(item);

        // 批量插入图片
        List<String> imageUrls = dto.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size(); i++) {
                ItemImage image = new ItemImage();
                image.setItemId(item.getId());
                image.setImageUrl(imageUrls.get(i));
                image.setSortOrder(i);
                image.setIsDeleted(0);
                itemImageMapper.insert(image);
            }
        }

        return buildDetailVO(item, imageUrls);
    }

    // ======================= 2. 商品详情 =======================
    public ItemDetailVO getDetail(Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null || isDeleted(item.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }

        // 浏览量 +1
        itemMapper.incrementViewCount(itemId);
        item.setViewCount(item.getViewCount() == null ? 1 : item.getViewCount() + 1);

        // 查图片
        List<String> imageUrls = getImageUrlsByItemId(itemId);

        return buildDetailVO(item, imageUrls);
    }

    // ======================= 3. 搜索商品 =======================
    public Page<ItemCardVO> search(ItemSearchDTO dto, int page, int size) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getIsDeleted, 0)
               .eq(Item::getStatus, STATUS_ON_SALE);

        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(Item::getTitle, dto.getKeyword())
                              .or()
                              .like(Item::getDescription, dto.getKeyword()));
        }
        if (dto.getCategoryId() != null) {
            wrapper.eq(Item::getCategoryId, dto.getCategoryId());
        }
        if (dto.getCampus() != null && !dto.getCampus().isBlank()) {
            wrapper.eq(Item::getCampus, dto.getCampus());
        }
        if (dto.getConditionLevel() != null && !dto.getConditionLevel().isBlank()) {
            wrapper.eq(Item::getConditionLevel, dto.getConditionLevel());
        }
        if (dto.getMinPrice() != null) {
            wrapper.ge(Item::getPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            wrapper.le(Item::getPrice, dto.getMaxPrice());
        }

        if ("asc".equalsIgnoreCase(dto.getPriceSort())) {
            wrapper.orderByAsc(Item::getPrice);
        } else if ("desc".equalsIgnoreCase(dto.getPriceSort())) {
            wrapper.orderByDesc(Item::getPrice);
        } else {
            wrapper.orderByDesc(Item::getCreateTime);
        }

        Page<Item> itemPage = new Page<>(page, size);
        itemPage = itemMapper.selectPage(itemPage, wrapper);

        // Entity → VO
        return buildCardVOPage(page, size, itemPage);
    }

    // ======================= 4. 用户商品列表 =======================
    public Page<ItemCardVO> listByUser(Long userId, String status, int page, int size) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getIsDeleted, 0)
               .eq(Item::getUserId, userId);

        if (status != null && !status.isBlank()) {
            wrapper.eq(Item::getStatus, status);
        }

        wrapper.orderByDesc(Item::getCreateTime);

        Page<Item> itemPage = new Page<>(page, size);
        itemPage = itemMapper.selectPage(itemPage, wrapper);
        return buildCardVOPage(page, size, itemPage);
    }

    private Page<ItemCardVO> buildCardVOPage(int page, int size, Page<Item> itemPage) {
        Page<ItemCardVO> voPage = new Page<>(page, size, itemPage.getTotal());
        List<ItemCardVO> voList = new ArrayList<>();
        for (Item item : itemPage.getRecords()) {
            voList.add(buildCardVO(item));
        }
        voPage.setRecords(voList);
        return voPage;
    }

    // ======================= 5. 编辑商品 =======================
    @Transactional(rollbackFor = Exception.class)
    public void update(Long itemId, ItemPublishDTO dto) {
        Item item = itemMapper.selectById(itemId);
        if (item == null || isDeleted(item.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }
        validateOwner(item, dto.getUserId());
        validateCategory(dto.getCategoryId());

        item.setTitle(dto.getTitle());
        item.setCampus(dto.getCampus());
        item.setConditionLevel(dto.getConditionLevel());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setCategoryId(dto.getCategoryId());
        // updateTime 由 MyMetaObjectHandler 自动填充

        itemMapper.updateById(item);

        // 图片：先删旧图，再插新图
        itemImageMapper.deleteByItemId(itemId);
        List<String> imageUrls = dto.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size(); i++) {
                ItemImage image = new ItemImage();
                image.setItemId(itemId);
                image.setImageUrl(imageUrls.get(i));
                image.setSortOrder(i);
                image.setIsDeleted(0);
                itemImageMapper.insert(image);
            }
        }
    }

    // ======================= 6. 下架商品 =======================
    public void offline(Long itemId, Long userId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null || isDeleted(item.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }
        validateOwner(item, userId);
        item.setStatus(STATUS_REMOVED);
        itemMapper.updateById(item);
    }

    // ======================= 7. 重新上架商品 =======================
    public void online(Long itemId, Long userId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null || isDeleted(item.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }
        validateOwner(item, userId);
        if (STATUS_SOLD.equals(item.getStatus())) {
            throw new BusinessException("已售商品不能重新上架");
        }
        item.setStatus(STATUS_ON_SALE);
        itemMapper.updateById(item);
    }

    // ======================= 辅助方法 =======================
    private List<String> getImageUrlsByItemId(Long itemId) {
        List<ItemImage> images = itemImageMapper.selectByItemId(itemId);
        List<String> urls = new ArrayList<>();
        for (ItemImage img : images) {
            urls.add(img.getImageUrl());
        }
        return urls;
    }

    private ItemDetailVO buildDetailVO(Item item, List<String> imageUrls) {
        ItemDetailVO vo = new ItemDetailVO();
        vo.setId(item.getId());
        vo.setUserId(item.getUserId());
        vo.setTitle(item.getTitle());
        vo.setCampus(item.getCampus());
        vo.setConditionLevel(item.getConditionLevel());
        vo.setDescription(item.getDescription());
        vo.setPrice(item.getPrice());
        vo.setCategoryId(item.getCategoryId());
        vo.setStatus(item.getStatus());
        vo.setViewCount(item.getViewCount());
        vo.setFavoriteCount(item.getFavoriteCount());
        vo.setImageUrls(imageUrls);
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    private ItemCardVO buildCardVO(Item item) {
        ItemCardVO vo = new ItemCardVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setCampus(item.getCampus());
        vo.setPrice(item.getPrice());
        vo.setConditionLevel(item.getConditionLevel());
        vo.setStatus(item.getStatus());
        vo.setViewCount(item.getViewCount());
        vo.setFavoriteCount(item.getFavoriteCount());
        vo.setCreateTime(item.getCreateTime());
        // 封面图：取第一张图片
        List<String> urls = getImageUrlsByItemId(item.getId());
        if (!urls.isEmpty()) {
            vo.setImageUrl(urls.get(0));
        }
        return vo;
    }

    private void validateCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || isDeleted(category.getIsDeleted()) || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException("商品分类不存在或已禁用");
        }
    }

    private void validateOwner(Item item, Long userId) {
        if (userId == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该商品");
        }
    }

    private boolean isDeleted(Integer isDeleted) {
        return isDeleted != null && isDeleted == 1;
    }
}
