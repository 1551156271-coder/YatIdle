package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.ItemImage;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.mapper.ItemImageMapper;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.vo.admin.AdminItemVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminItemService {

    private final ItemMapper itemMapper;
    private final ItemImageMapper itemImageMapper;
    private final UserMapper userMapper;
    private final AdminLogService adminLogService;
    private final String baseUrl;

    public AdminItemService(ItemMapper itemMapper,
                            ItemImageMapper itemImageMapper,
                            UserMapper userMapper,
                            AdminLogService adminLogService,
                            @Value("${app.base-url}") String baseUrl) {
        this.itemMapper = itemMapper;
        this.itemImageMapper = itemImageMapper;
        this.userMapper = userMapper;
        this.adminLogService = adminLogService;
        this.baseUrl = baseUrl;
    }

    public Page<AdminItemVO> list(String keyword, Long categoryId, String status, String campus, int page, int size) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getIsDeleted, 0);
        if (keyword != null && !keyword.isBlank()) wrapper.like(Item::getTitle, keyword);
        if (categoryId != null) wrapper.eq(Item::getCategoryId, categoryId);
        if (status != null && !status.isBlank()) wrapper.eq(Item::getStatus, status);
        if (campus != null && !campus.isBlank()) wrapper.eq(Item::getCampus, campus);
        wrapper.orderByDesc(Item::getCreateTime);
        Page<Item> source = itemMapper.selectPage(new Page<>(page, size), wrapper);
        Page<AdminItemVO> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setPages(source.getPages());
        result.setRecords(enrichItems(source.getRecords()));
        return result;
    }

    public Map<String, Object> detail(Long itemId) {
        Item item = findActiveItem(itemId);
        Map<String, Object> data = new HashMap<>();
        data.put("item", item);
        data.put("images", imageUrls(itemId));
        User seller = userMapper == null ? null : userMapper.selectById(item.getUserId());
        if (seller != null) seller.setPassword(null);
        data.put("seller", seller);
        return data;
    }

    public void updateStatus(Long adminId, Long itemId, String status, String reason) {
        if (!"ON_SALE".equals(status) && !"SOLD".equals(status) && !"REMOVED".equals(status)) throw new BusinessException("商品状态不合法");
        requireReason(reason);
        Item item = findActiveItem(itemId);
        String before = item.getStatus();
        item.setStatus(status);
        itemMapper.updateById(item);
        adminLogService.log(adminId, "UPDATE_ITEM_STATUS", "ITEM", itemId, before, status, reason);
    }

    public void delete(Long adminId, Long itemId, String reason) {
        requireReason(reason);
        Item item = findActiveItem(itemId);
        String before = item.getStatus();
        item.setIsDeleted(1);
        itemMapper.updateById(item);
        adminLogService.log(adminId, "DELETE_ITEM", "ITEM", itemId, before, "DELETED", reason);
    }

    private Item findActiveItem(Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null || (item.getIsDeleted() != null && item.getIsDeleted() == 1)) throw new BusinessException("商品不存在");
        return item;
    }

    private List<String> imageUrls(Long itemId) {
        if (itemImageMapper == null) return List.of();
        return itemImageMapper.selectByItemId(itemId).stream().map(ItemImage::getImageUrl).map(this::resolveUrl).toList();
    }

    private List<AdminItemVO> enrichItems(List<Item> items) {
        Map<Long, String> coverImages = coverImages(items);
        return items.stream().map(AdminItemVO::from).peek(vo -> vo.setImageUrl(coverImages.get(vo.getId()))).toList();
    }

    private Map<Long, String> coverImages(List<Item> items) {
        if (itemImageMapper == null || items.isEmpty()) return Map.of();
        Set<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, String> covers = new LinkedHashMap<>();
        for (Long itemId : itemIds) {
            List<String> urls = imageUrls(itemId);
            if (!urls.isEmpty()) covers.put(itemId, urls.get(0));
        }
        return covers;
    }

    private String resolveUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        return baseUrl + url;
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank()) throw new BusinessException("操作原因不能为空");
    }
}
