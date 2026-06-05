package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.dto.admin.AdminCategoryDTO;
import com.yatidle.backend.entity.Category;
import com.yatidle.backend.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminCategoryService {
    private final CategoryMapper categoryMapper;
    private final AdminLogService adminLogService;

    public AdminCategoryService(CategoryMapper categoryMapper, AdminLogService adminLogService) {
        this.categoryMapper = categoryMapper;
        this.adminLogService = adminLogService;
    }

    public List<Category> list() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().eq(Category::getIsDeleted, 0).orderByAsc(Category::getSortOrder));
    }

    public Category create(Long adminId, AdminCategoryDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) throw new BusinessException("分类名称不能为空");
        Category category = new Category();
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        Integer status = dto.getStatus() == null ? 1 : dto.getStatus();
        validateStatus(status);
        category.setStatus(status);
        category.setIsDeleted(0);
        categoryMapper.insert(category);
        adminLogService.log(adminId, "CREATE_CATEGORY", "CATEGORY", category.getId(), null, String.valueOf(category.getStatus()), category.getName());
        return category;
    }

    public Category update(Long adminId, Long id, AdminCategoryDTO dto) {
        Category category = detail(id);
        String before = String.valueOf(category.getStatus());
        if (dto.getName() != null) category.setName(dto.getName());
        if (dto.getSortOrder() != null) category.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) {
            validateStatus(dto.getStatus());
            category.setStatus(dto.getStatus());
        }
        categoryMapper.updateById(category);
        adminLogService.log(adminId, "UPDATE_CATEGORY", "CATEGORY", id, before, String.valueOf(category.getStatus()), category.getName());
        return category;
    }

    public void delete(Long adminId, Long id) {
        Category category = detail(id);
        String before = String.valueOf(category.getStatus());
        category.setIsDeleted(1);
        categoryMapper.updateById(category);
        adminLogService.log(adminId, "DELETE_CATEGORY", "CATEGORY", id, before, "DELETED", category.getName());
    }

    private Category detail(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null || (category.getIsDeleted() != null && category.getIsDeleted() == 1)) throw new BusinessException("分类不存在");
        return category;
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) throw new BusinessException("分类状态不合法");
    }
}
