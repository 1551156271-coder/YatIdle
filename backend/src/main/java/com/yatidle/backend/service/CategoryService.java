package com.yatidle.backend.service;

import com.yatidle.backend.entity.Category;
import com.yatidle.backend.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<Category> listAllActive() {
        return categoryMapper.selectAllActive();
    }
}
