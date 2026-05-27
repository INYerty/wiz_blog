package com.wizv.blog.service.impl;

import com.wizv.blog.entity.Category;
import com.wizv.blog.mapper.CategoryMapper;
import com.wizv.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectList(null);
    }
}
