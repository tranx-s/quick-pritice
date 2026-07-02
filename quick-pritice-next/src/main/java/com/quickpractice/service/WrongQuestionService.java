package com.quickpractice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickpractice.entity.Question;
import com.quickpractice.entity.WrongQuestion;
import com.quickpractice.mapper.WrongQuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WrongQuestionService {

    private final WrongQuestionMapper wrongQuestionMapper;
    @Lazy
    private final QuestionService questionService;

    public void addWrongQuestion(String openid, Long questionId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getOpenid, openid)
               .eq(WrongQuestion::getQuestionId, questionId);
        WrongQuestion existing = wrongQuestionMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setWrongCount(existing.getWrongCount() + 1);
            wrongQuestionMapper.updateById(existing);
        } else {
            WrongQuestion wq = new WrongQuestion();
            wq.setOpenid(openid);
            wq.setQuestionId(questionId);
            wq.setWrongCount(1);
            wrongQuestionMapper.insert(wq);
        }
    }

    public List<Map<String, Object>> getWrongListWithDetail(String openid) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getOpenid, openid)
               .orderByDesc(WrongQuestion::getUpdatedTime);
        List<WrongQuestion> wrongList = wrongQuestionMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (WrongQuestion wq : wrongList) {
            Question q = questionService.getById(wq.getQuestionId());
            if (q == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("wrongId", wq.getId());
            item.put("wrongCount", wq.getWrongCount());
            item.put("updatedTime", wq.getUpdatedTime());
            item.put("question", q);
            result.add(item);
        }
        return result;
    }

    public void deleteWrong(String openid, Long questionId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getOpenid, openid)
               .eq(WrongQuestion::getQuestionId, questionId);
        wrongQuestionMapper.delete(wrapper);
    }
}
