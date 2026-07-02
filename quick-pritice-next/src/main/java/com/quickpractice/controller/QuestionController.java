package com.quickpractice.controller;

import com.quickpractice.common.Result;
import com.quickpractice.entity.Question;
import com.quickpractice.service.QuestionService;
import com.quickpractice.service.UserService;
import com.quickpractice.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 题目控制器
 */
@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final WrongQuestionService wrongQuestionService;
    private final UserService userService;

    /**
     * 极速速刷 - 随机获取10道题
     */
    @GetMapping("/random")
    public Result<List<Question>> getRandomQuestions(
            @RequestParam(defaultValue = "10") Integer count,
            @RequestParam(required = false) String moduleType) {

        List<Question> questions = questionService.getRandomQuestions(count, moduleType);
        return Result.success(questions);
    }

    /**
     * 提交答题结果
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitAnswer(@RequestBody Map<String, Object> params) {
        String openid = (String) params.get("openid");
        Long questionId = Long.valueOf(params.get("questionId").toString());
        String userAnswer = (String) params.get("userAnswer");

        // 获取题目正确答案
        Question question = questionService.getById(questionId);
        if (question == null) {
            return Result.error("题目不存在");
        }

        boolean isCorrect = question.getCorrectAnswer().equals(userAnswer);

        // 答错自动收录错题本
        if (!isCorrect) {
            wrongQuestionService.addWrongQuestion(openid, questionId);
        }

        // 增加刷题数
        userService.increaseQuestionCount(openid, 1);

        Map<String, Object> resultData = Map.of(
            "isCorrect", isCorrect,
            "correctAnswer", question.getCorrectAnswer(),
            "analysis", question.getAnalysis() != null ? question.getAnalysis() : ""
        );

        return Result.success(resultData);
    }

    /**
     * 根据ID获取题目详情
     */
    @GetMapping("/{id}")
    public Result<Question> getQuestionById(@PathVariable Long id) {
        Question question = questionService.getById(id);
        if (question == null) {
            return Result.error("题目不存在");
        }
        return Result.success(question);
    }
}
