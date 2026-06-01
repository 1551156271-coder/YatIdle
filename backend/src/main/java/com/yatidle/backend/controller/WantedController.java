package com.yatidle.backend.controller;

import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.wanted.CreateWantedDTO;
import com.yatidle.backend.service.WantedService;
import com.yatidle.backend.vo.wanted.WantedDetailVO;
import com.yatidle.backend.vo.wanted.WantedVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/wanted")
public class WantedController {

    private final WantedService wantedService;

    public WantedController(WantedService wantedService) {
        this.wantedService = wantedService;
    }

    @PostMapping
    public Result<WantedDetailVO> create(@RequestBody CreateWantedDTO dto) {
        return Result.success(wantedService.create(dto));
    }

    @GetMapping("/list")
    public Result<List<WantedVO>> list(@RequestParam(required = false) String campus,
                                        @RequestParam(required = false) Long categoryId,
                                        @RequestParam(required = false) String status) {
        return Result.success(wantedService.list(campus, categoryId, status));
    }

    @GetMapping("/{id}")
    public Result<WantedDetailVO> getById(@PathVariable Long id) {
        return Result.success(wantedService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CreateWantedDTO dto) {
        wantedService.update(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        wantedService.deleteById(id);
        return Result.success(null);
    }

    @DeleteMapping("/user/{userId}")
    public Result<Void> deleteAllByUserId(@PathVariable Long userId) {
        wantedService.deleteAllByUserId(userId);
        return Result.success(null);
    }

    @GetMapping("/my")
    public Result<List<WantedVO>> myWanted(@RequestParam Long userId) {
        return Result.success(wantedService.myWanted(userId));
    }

    @PutMapping("/{id}/close")
    public Result<Void> closeWanted(@PathVariable Long id, @RequestParam Long userId) {
        wantedService.closeWanted(id, userId);
        return Result.success(null);
    }
}
