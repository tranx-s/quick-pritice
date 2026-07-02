package com.quickpractice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quickpractice.entity.Question;
import com.quickpractice.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionMapper questionMapper;

    public List<Question> getRandomQuestions(Integer count, String moduleType) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getIsOnline, 1);
        if (moduleType != null && !moduleType.isEmpty()) {
            wrapper.eq(Question::getModuleType, moduleType);
        }
        List<Question> all = questionMapper.selectList(wrapper);
        Collections.shuffle(all);
        return all.subList(0, Math.min(count, all.size()));
    }

    public Question getById(Long id) {
        return questionMapper.selectById(id);
    }

    /** 旧接口，兼容小程序端调用 */
    public Page<Question> getQuestionPage(Integer page, Integer size, String moduleType) {
        return getQuestionPage(page, size, moduleType, null, null, null);
    }

    /** 管理端分页查询，支持多条件筛选 */
    public Page<Question> getQuestionPage(Integer page, Integer size,
                                           String moduleType, String source,
                                           Integer isOnline, String keyword) {
        Page<Question> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (moduleType != null && !moduleType.isEmpty()) {
            wrapper.eq(Question::getModuleType, moduleType);
        }
        if (source != null && !source.isEmpty()) {
            wrapper.eq(Question::getSource, source);
        }
        if (isOnline != null) {
            wrapper.eq(Question::getIsOnline, isOnline);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Question::getContent, keyword);
        }
        wrapper.orderByDesc(Question::getId);
        return questionMapper.selectPage(pageParam, wrapper);
    }

    public void addQuestion(Question question) {
        if (question.getIsOnline() == null) question.setIsOnline(1);
        if (question.getSource() == null) question.setSource("manual");
        questionMapper.insert(question);
    }

    public void updateQuestion(Question question) {
        questionMapper.updateById(question);
    }

    public void deleteQuestion(Long id) {
        questionMapper.deleteById(id);
    }

    public void toggleOnline(Long id) {
        Question q = questionMapper.selectById(id);
        if (q != null) {
            q.setIsOnline(q.getIsOnline() == 1 ? 0 : 1);
            questionMapper.updateById(q);
        }
    }

    public Map<String, Object> getStats() {
        long total = questionMapper.selectCount(new LambdaQueryWrapper<Question>());
        long online = questionMapper.selectCount(new LambdaQueryWrapper<Question>().eq(Question::getIsOnline, 1));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("online", online);
        result.put("offline", total - online);
        return result;
    }

    public long countByModule(String moduleType) {
        return questionMapper.selectCount(
            new LambdaQueryWrapper<Question>()
                .eq(Question::getIsOnline, 1)
                .eq(Question::getModuleType, moduleType)
        );
    }
}

