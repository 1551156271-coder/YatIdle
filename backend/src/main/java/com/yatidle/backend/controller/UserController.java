package com.yatidle.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yatidle.backend.common.JwtUtils;
import com.yatidle.backend.common.Result;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.dto.user.LoginDTO;
import com.yatidle.backend.dto.user.RegisterDTO;
import com.yatidle.backend.dto.user.UpdateProfileDTO;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.Review;
import com.yatidle.backend.entity.TradeOrder;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.ReviewMapper;
import com.yatidle.backend.mapper.TradeOrderMapper;
import com.yatidle.backend.service.UserService;
import com.yatidle.backend.util.ImageUploadValidator;
import com.yatidle.backend.vo.user.LoginVO;
import com.yatidle.backend.vo.user.UserVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final String baseUrl;
    private final TradeOrderMapper tradeOrderMapper;
    private final ItemMapper itemMapper;
    private final ReviewMapper reviewMapper;

    public UserController(UserService userService, JwtUtils jwtUtils,
                          @Value("${app.base-url}") String baseUrl,
                          TradeOrderMapper tradeOrderMapper,
                          ItemMapper itemMapper,
                          ReviewMapper reviewMapper) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.baseUrl = baseUrl;
        this.tradeOrderMapper = tradeOrderMapper;
        this.itemMapper = itemMapper;
        this.reviewMapper = reviewMapper;
    }

    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody RegisterDTO dto) {
        User user = userService.register(dto.getUsername(), dto.getPassword(),
                dto.getNickname(), dto.getBio(), dto.getCampus());
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtils.generateToken(user.getId()));
        vo.setUser(UserVO.from(user, baseUrl));
        return Result.success(vo);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        User user = userService.login(dto.getUsername(), dto.getPassword());
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtils.generateToken(user.getId()));
        vo.setUser(UserVO.from(user, baseUrl));
        return Result.success(vo);
    }

    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = UserVO.from(user, baseUrl);

        // 成交数：作为买家或卖家的已完成订单
        long dealCount = tradeOrderMapper.selectCount(
            new LambdaQueryWrapper<TradeOrder>()
                .and(w -> w.eq(TradeOrder::getBuyerId, id).or().eq(TradeOrder::getSellerId, id))
                .eq(TradeOrder::getStatus, "COMPLETED")
                .eq(TradeOrder::getIsDeleted, 0)
        );
        vo.setDealCount((int) dealCount);

        // 在售商品数
        long goodsCount = itemMapper.selectCount(
            new LambdaQueryWrapper<Item>()
                .eq(Item::getUserId, id)
                .eq(Item::getStatus, "ON_SALE")
                .eq(Item::getIsDeleted, 0)
        );
        vo.setGoodsCount((int) goodsCount);

        // 收到评价数
        long reviewCount = reviewMapper.selectCount(
            new LambdaQueryWrapper<Review>()
                .eq(Review::getRevieweeId, id)
                .eq(Review::getIsDeleted, 0)
        );
        vo.setReviewCount((int) reviewCount);

        return Result.success(vo);
    }

    @GetMapping("/list")
    public Result<List<UserVO>> findAll() {
        List<User> users = userService.findAll();
        List<UserVO> voList = users.stream().map(u -> UserVO.from(u, baseUrl)).collect(Collectors.toList());
        return Result.success(voList);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return Result.success(null);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UpdateProfileDTO dto) {
        userService.updateProfile(dto.getUserId(), dto.getPassword(), dto.getPhone(), dto.getAvatar(),
                dto.getNickname(), dto.getBio(), dto.getCampus());
        return Result.success(null);
    }

    @PostMapping("/avatar/upload")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        String ext = ImageUploadValidator.validate(file);
        String filename = UUID.randomUUID() + ext;
        Path uploadDir = Paths.get("uploads", "avatar").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(filename);
        file.transferTo(target);

        return Result.success(Map.of("url", "/uploads/avatar/" + filename));
    }

    @GetMapping("/wallet")
    public Result<Map<String, Object>> getWallet(@RequestParam Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Map<String, Object> data = new HashMap<>();

        List<TradeOrder> incomeOrders = tradeOrderMapper.selectList(
            new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getSellerId, userId)
                .eq(TradeOrder::getStatus, "COMPLETED")
                .eq(TradeOrder::getIsDeleted, 0)
        );

        BigDecimal totalIncome = incomeOrders.stream()
                .map(TradeOrder::getPrice)
                        .filter(p -> p != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TradeOrder> expenseOrders = tradeOrderMapper.selectList(
            new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getBuyerId, userId)
                .eq(TradeOrder::getStatus, "COMPLETED")
                .eq(TradeOrder::getIsDeleted, 0)
        );

        BigDecimal totalExpense = expenseOrders.stream()
                .map(TradeOrder::getPrice)
                        .filter(p -> p != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

        data.put("balance", user.getBalance());
        data.put("totalIncome", totalIncome);
        data.put("totalExpense", totalExpense);
        return Result.success(data);
    }
}
