package com.yatidle.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yatidle.backend.entity.Favorite;
import com.yatidle.backend.mapper.FavoriteMapper;
import org.springframework.stereotype.Service;

@Service
public class FavoriteService extends ServiceImpl<FavoriteMapper, Favorite> {
}
