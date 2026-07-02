package com.quickpractice.controller;

import com.quickpractice.common.Result;
import com.quickpractice.entity.Question;
import com.quickpractice.service.QuestionService;
import com.quickpractice.service.UserService;
import com.quickpractice.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
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

    /**
     * 专项刷题 - 模块列表及各模块题量
     */
    @GetMapping("/module/list")
    public Result<List<Map<String, Object>>> moduleList() {
        List<String> moduleTypes = Arrays.asList("言语理解", "数量关系", "判断推理", "政治理论");
        List<Map<String, Object>> list = moduleTypes.stream().map(type -> {
            long count = questionService.countByModule(type);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("moduleType", type);
            item.put("count", count);
            return item;
        }).toList();
        return Result.success(list);
    }

    /**
     * 专项刷题 - 按模块取题
     */
    @GetMapping("/module")
    public Result<List<Question>> moduleQuestions(
            @RequestParam String moduleType,
            @RequestParam(defaultValue = "20") Integer count) {
        List<Question> questions = questionService.getRandomQuestions(count, moduleType);
        return Result.success(questions);
    }

    /**
     * 错题本 - 获取用户错题列表（带题目详情）
     */
    @GetMapping("/wrong/list")
    public Result<List<Map<String, Object>>> wrongList(@RequestParam String openid) {
        return Result.success(wrongQuestionService.getWrongListWithDetail(openid));
    }

    /**
     * 错题本 - 删除单条错题
     */
    @DeleteMapping("/wrong/{questionId}")
    public Result<Void> deleteWrong(@PathVariable Long questionId, @RequestParam String openid) {
        wrongQuestionService.deleteWrong(openid, questionId);
        return Result.success();
    }
}
