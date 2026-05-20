package com.yatidle.backend.controller;

import com.yatidle.backend.common.Result;
import com.yatidle.backend.entity.Category;
import com.yatidle.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> listAll() {
        List<Category> categories = categoryService.listAllActive();
        return Result.success(categories);
    }
}
