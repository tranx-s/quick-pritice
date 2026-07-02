package com.quickpractice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickpractice.entity.User;
import com.quickpractice.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 微信小程序登录（临时mock版本）
     */
    public User login(String code) {
        // TODO: 集成微信SDK后替换为真实登录
        String openid = "mock_openid_" + code;

        // 查询用户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        // 新用户自动注册
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setVipStatus(0);
            user.setTotalQuestions(0);
            user.setTodayQuestions(0);
            user.setCreatedTime(LocalDateTime.now());
            userMapper.insert(user);
        }

        return user;
    }

    /**
     * 更新用户信息
     */
    public void updateUserInfo(String openid, String nickname, String avatar) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        if (user != null) {
            user.setNickname(nickname);
            user.setAvatar(avatar);
            userMapper.updateById(user);
        }
    }

    /**
     * 增加刷题数
     */
    public void increaseQuestionCount(String openid, int count) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        if (user != null) {
            user.setTotalQuestions(user.getTotalQuestions() + count);
            user.setTodayQuestions(user.getTodayQuestions() + count);
            userMapper.updateById(user);
        }
    }

    /**
     * 检查是否为会员
     */
    public boolean isVip(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        if (user == null || user.getVipStatus() == 0) {
            return false;
        }

        // 检查会员是否过期
        if (user.getVipExpireTime() != null && user.getVipExpireTime().isBefore(LocalDateTime.now())) {
            user.setVipStatus(0);
            userMapper.updateById(user);
            return false;
        }

        return true;
    }

    /**
     * 根据openid获取用户
     */
    public User getUserByOpenid(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        return userMapper.selectOne(wrapper);
    }
}
