package com.yatidle.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @GetMapping("/test")
    public String test() {
        return "favorite module is OK";
    }
}
