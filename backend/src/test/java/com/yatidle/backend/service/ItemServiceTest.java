package com.yatidle.backend.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private ItemImageMapper itemImageMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Test
    void publishCreatesOnSaleItemAndImageRowsAfterValidCategory() {
        ItemService service = new ItemService(itemMapper, itemImageMapper, categoryMapper);
        when(categoryMapper.selectById(1L)).thenReturn(activeCategory());
        when(itemMapper.insert(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(99L);
            return 1;
        });

        ItemPublishDTO dto = publishDto(7L);
        dto.setImageUrls(List.of("/uploads/items/a.jpg", "/uploads/items/b.jpg"));

        ItemDetailVO result = service.publish(dto);

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemMapper).insert(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getStatus()).isEqualTo("ON_SALE");
        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getStatus()).isEqualTo("ON_SALE");
        assertThat(result.getImageUrls()).containsExactly("/uploads/items/a.jpg", "/uploads/items/b.jpg");

        ArgumentCaptor<ItemImage> imageCaptor = ArgumentCaptor.forClass(ItemImage.class);
        verify(itemImageMapper, org.mockito.Mockito.times(2)).insert(imageCaptor.capture());
        assertThat(imageCaptor.getAllValues())
                .extracting(ItemImage::getItemId, ItemImage::getSortOrder, ItemImage::getImageUrl)
                .containsExactly(
                        org.assertj.core.api.Assertions.tuple(99L, 0, "/uploads/items/a.jpg"),
                        org.assertj.core.api.Assertions.tuple(99L, 1, "/uploads/items/b.jpg")
                );
    }

    @Test
    void updateRejectsNonOwner() {
        ItemService service = new ItemService(itemMapper, itemImageMapper, categoryMapper);
        Item item = new Item();
        item.setId(11L);
        item.setUserId(1L);
        item.setIsDeleted(0);
        when(itemMapper.selectById(11L)).thenReturn(item);

        assertThatThrownBy(() -> service.update(11L, publishDto(2L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权操作该商品");
    }

    @Test
    void offlineAndOnlineRequireOwnerAndSwitchStatus() {
        ItemService service = new ItemService(itemMapper, itemImageMapper, categoryMapper);
        Item item = new Item();
        item.setId(11L);
        item.setUserId(1L);
        item.setIsDeleted(0);
        item.setStatus("ON_SALE");
        when(itemMapper.selectById(11L)).thenReturn(item);

        service.offline(11L, 1L);
        assertThat(item.getStatus()).isEqualTo("REMOVED");

        service.online(11L, 1L);
        assertThat(item.getStatus()).isEqualTo("ON_SALE");
    }

    @Test
    void listByUserReturnsStatusInCards() {
        ItemService service = new ItemService(itemMapper, itemImageMapper, categoryMapper);
        Item item = new Item();
        item.setId(11L);
        item.setUserId(1L);
        item.setTitle("二手书");
        item.setStatus("SOLD");
        item.setPrice(new BigDecimal("12.00"));
        item.setIsDeleted(0);

        Page<Item> itemPage = new Page<>(1, 10, 1);
        itemPage.setRecords(List.of(item));
        when(itemMapper.selectPage(any(Page.class), any())).thenReturn(itemPage);

        Page<ItemCardVO> result = service.listByUser(1L, "SOLD", 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(11L);
        assertThat(result.getRecords().get(0).getStatus()).isEqualTo("SOLD");
    }

    @Test
    void searchAcceptsConditionAndPriceSort() {
        ItemService service = new ItemService(itemMapper, itemImageMapper, categoryMapper);
        Page<Item> emptyPage = new Page<>(1, 20, 0);
        when(itemMapper.selectPage(any(Page.class), any())).thenReturn(emptyPage);

        ItemSearchDTO dto = new ItemSearchDTO();
        dto.setConditionLevel("95新");
        dto.setPriceSort("desc");

        Page<ItemCardVO> result = service.search(dto, 1, 20);

        assertThat(result.getRecords()).isEmpty();
        verify(itemMapper).selectPage(any(Page.class), any());
    }

    private static ItemPublishDTO publishDto(Long userId) {
        ItemPublishDTO dto = new ItemPublishDTO();
        dto.setUserId(userId);
        dto.setTitle("二手 iPad");
        dto.setCampus("南校园");
        dto.setConditionLevel("95新");
        dto.setDescription("自用");
        dto.setPrice(new BigDecimal("1580.00"));
        dto.setCategoryId(1L);
        return dto;
    }

    private static Category activeCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("数码电子");
        category.setStatus(1);
        category.setIsDeleted(0);
        return category;
    }
}
