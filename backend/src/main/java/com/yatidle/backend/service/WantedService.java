package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.dto.wanted.CreateWantedDTO;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.entity.Wanted;
import com.yatidle.backend.entity.WantedImage;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.mapper.WantedImageMapper;
import com.yatidle.backend.mapper.WantedMapper;
import com.yatidle.backend.vo.wanted.WantedDetailVO;
import com.yatidle.backend.vo.wanted.WantedVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WantedService {

    private final WantedMapper wantedMapper;
    private final WantedImageMapper wantedImageMapper;
    private final UserMapper userMapper;
    private final String baseUrl;

    public WantedService(WantedMapper wantedMapper,
                         WantedImageMapper wantedImageMapper,
                         UserMapper userMapper,
                         @Value("${app.base-url}") String baseUrl) {
        this.wantedMapper = wantedMapper;
        this.wantedImageMapper = wantedImageMapper;
        this.userMapper = userMapper;
        this.baseUrl = baseUrl;
    }

    public WantedDetailVO create(CreateWantedDTO dto) {
        Wanted wanted = new Wanted();
        wanted.setUserId(dto.getUserId());
        wanted.setTitle(dto.getTitle());
        wanted.setBudgetMin(dto.getBudgetMin());
        wanted.setBudgetMax(dto.getBudgetMax());
        wanted.setCampus(dto.getCampus());
        wanted.setConditionLevel(dto.getConditionLevel());
        wanted.setDescription(dto.getDescription());
        wanted.setCategoryId(dto.getCategoryId());
        wanted.setStatus("active");
        wanted.setViewCount(0);
        wanted.setIsDeleted(0);
        wantedMapper.insert(wanted);

        List<String> imageUrls = Collections.emptyList();
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            imageUrls = dto.getImageUrls();
            for (int i = 0; i < imageUrls.size(); i++) {
                WantedImage img = new WantedImage();
                img.setWantedId(wanted.getId());
                img.setImageUrl(imageUrls.get(i));
                img.setSortOrder(i);
                wantedImageMapper.insert(img);
            }
        }
        WantedDetailVO detailVO = WantedDetailVO.from(wanted, imageUrls);
        detailVO.setUsername(getUsername(wanted.getUserId()));
        detailVO.setNickname(getUsername(wanted.getUserId()));
        detailVO.setAvatar(getAvatar(wanted.getUserId()));
        return detailVO;
    }

    public List<WantedVO> list(String campus, Long categoryId, String status) {
        LambdaQueryWrapper<Wanted> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wanted::getIsDeleted, 0);
        if (campus != null && !campus.isEmpty()) {
            wrapper.eq(Wanted::getCampus, campus);
        }
        if (categoryId != null) {
            wrapper.eq(Wanted::getCategoryId, categoryId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Wanted::getStatus, status);
        }
        wrapper.orderByDesc(Wanted::getCreateTime);
        List<Wanted> list = wantedMapper.selectList(wrapper);
        return list.stream().map(w -> {
            WantedVO vo = WantedVO.from(w);
            vo.setUsername(getUsername(w.getUserId()));
            vo.setNickname(getUsername(w.getUserId()));
            vo.setAvatar(getAvatar(w.getUserId()));
            return vo;
        }).collect(Collectors.toList());
    }

    public WantedDetailVO getById(Long id) {
        Wanted wanted = wantedMapper.selectById(id);
        if (wanted == null) {
            throw new BusinessException("求购信息不存在");
        }
        wanted.setViewCount(wanted.getViewCount() + 1);
        wantedMapper.updateById(wanted);
        List<WantedImage> images = wantedImageMapper.selectByWantedId(id);
        List<String> imageUrls = images.stream().map(WantedImage::getImageUrl).map(this::resolveUrl).collect(Collectors.toList());
        WantedDetailVO vo = WantedDetailVO.from(wanted, imageUrls);
        vo.setUsername(getUsername(wanted.getUserId()));
        vo.setNickname(getUsername(wanted.getUserId()));
        vo.setAvatar(getAvatar(wanted.getUserId()));
        return vo;
    }

    public int update(Long id, CreateWantedDTO dto) {
        Wanted wanted = wantedMapper.selectById(id);
        if (wanted == null) {
            throw new BusinessException("求购信息不存在");
        }
        if (dto.getTitle() != null) wanted.setTitle(dto.getTitle());
        if (dto.getBudgetMin() != null) wanted.setBudgetMin(dto.getBudgetMin());
        if (dto.getBudgetMax() != null) wanted.setBudgetMax(dto.getBudgetMax());
        if (dto.getCampus() != null) wanted.setCampus(dto.getCampus());
        if (dto.getConditionLevel() != null) wanted.setConditionLevel(dto.getConditionLevel());
        if (dto.getDescription() != null) wanted.setDescription(dto.getDescription());
        if (dto.getCategoryId() != null) wanted.setCategoryId(dto.getCategoryId());
        if (dto.getImageUrls() != null) {
            wantedImageMapper.deleteByWantedId(id);
            for (int i = 0; i < dto.getImageUrls().size(); i++) {
                WantedImage img = new WantedImage();
                img.setWantedId(id);
                img.setImageUrl(dto.getImageUrls().get(i));
                img.setSortOrder(i);
                wantedImageMapper.insert(img);
            }
        }
        return wantedMapper.updateById(wanted);
    }

    public int deleteById(Long id) {
        wantedImageMapper.deleteByWantedId(id);
        return wantedMapper.deleteById(id);
    }

    public void closeWanted(Long id, Long userId) {
        Wanted wanted = wantedMapper.selectById(id);
        if (wanted == null) {
            throw new BusinessException("求购信息不存在");
        }
        if (!wanted.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该求购");
        }
        wanted.setStatus("closed");
        wantedMapper.updateById(wanted);
    }

    public int deleteAllByUserId(Long userId) {
        wantedImageMapper.deleteByUserId(userId);
        return wantedMapper.deleteByUserId(userId);
    }

    public List<WantedVO> myWanted(Long userId) {
        List<Wanted> list = wantedMapper.selectList(
                new LambdaQueryWrapper<Wanted>()
                        .eq(Wanted::getUserId, userId)
                        .eq(Wanted::getIsDeleted, 0)
                        .orderByDesc(Wanted::getCreateTime)
        );
        return list.stream().map(w -> {
            WantedVO vo = WantedVO.from(w);
            vo.setUsername(getUsername(w.getUserId()));
            vo.setNickname(getUsername(w.getUserId()));
            vo.setAvatar(getAvatar(w.getUserId()));
            return vo;
        }).collect(Collectors.toList());
    }

    private String getUsername(Long userId) {
        if (userId == null) return null;
        User user = userMapper.selectById(userId);
        return user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : null;
    }

    private String getAvatar(Long userId) {
        if (userId == null) return null;
        User user = userMapper.selectById(userId);
        return user != null ? resolveUrl(user.getAvatar()) : null;
    }

    private String resolveUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        return baseUrl + url;
    }
}
