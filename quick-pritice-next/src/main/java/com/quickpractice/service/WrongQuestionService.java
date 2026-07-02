package com.quickpractice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickpractice.entity.WrongQuestion;
import com.quickpractice.mapper.WrongQuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 错题服务
 */
@Service
@RequiredArgsConstructor
public class WrongQuestionService {

    private final WrongQuestionMapper wrongQuestionMapper;

    /**
     * 添加错题
     */
    public void addWrongQuestion(String openid, Long questionId) {
        // 查询是否已存在
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getOpenid, openid)
               .eq(WrongQuestion::getQuestionId, questionId);

        WrongQuestion existing = wrongQuestionMapper.selectOne(wrapper);

        if (existing != null) {
            // 已存在则增加错误次数
            existing.setWrongCount(existing.getWrongCount() + 1);
            wrongQuestionMapper.updateById(existing);
        } else {
            // 不存在则新增
            WrongQuestion wrongQuestion = new WrongQuestion();
            wrongQuestion.setOpenid(openid);
            wrongQuestion.setQuestionId(questionId);
            wrongQuestion.setWrongCount(1);
            wrongQuestionMapper.insert(wrongQuestion);
        }
    }
}
