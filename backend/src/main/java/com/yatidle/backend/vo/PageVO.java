package com.yatidle.backend.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {

    private Long total;

    private Long pageNum;

    private Long pageSize;

    private List<T> records;
}
