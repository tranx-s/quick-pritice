package com.quickpractice.controller;

import com.quickpractice.common.Result;
import com.quickpractice.entity.User;
import com.quickpractice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 微信小程序登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        if (code == null || code.isEmpty()) {
            return Result.error("code不能为空");
        }

        User user = userService.login(code);

        Map<String, Object> data = new HashMap<>();
        data.put("openid", user.getOpenid());
        data.put("isVip", user.getVipStatus() == 1);
        data.put("vipExpireTime", user.getVipExpireTime());
        data.put("totalQuestions", user.getTotalQuestions());
        data.put("todayQuestions", user.getTodayQuestions());

        return Result.success(data);
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    public Result<Void> updateUserInfo(@RequestBody Map<String, String> params) {
        String openid = params.get("openid");
        String nickname = params.get("nickname");
        String avatar = params.get("avatar");

        userService.updateUserInfo(openid, nickname, avatar);
        return Result.success();
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestParam String openid) {
        User user = userService.getUserByOpenid(openid);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }
}
