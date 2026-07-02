package com.quickpractice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quickpractice.common.Result;
import com.quickpractice.entity.Question;
import com.quickpractice.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/question")
@RequiredArgsConstructor
public class AdminQuestionController {

    private final QuestionService questionService;

    @GetMapping("/page")
    public Result<Page<Question>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String moduleType,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer isOnline,
            @RequestParam(required = false) String keyword) {
        return Result.success(questionService.getQuestionPage(page, size, moduleType, source, isOnline, keyword));
    }

    @GetMapping("/module-types")
    public Result<List<String>> moduleTypes() {
        return Result.success(Arrays.asList("言语理解", "数量关系", "判断推理", "政治理论"));
    }

    @GetMapping("/sources")
    public Result<List<String>> sources() {
        return Result.success(Arrays.asList("manual", "fenbi", "offcn", "huatu", "yuantiku"));
    }

    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        Question q = questionService.getById(id);
        if (q == null) return Result.error("题目不存在");
        return Result.success(q);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Question question) {
        if (question.getSource() == null) question.setSource("manual");
        if (question.getIsOnline() == null) question.setIsOnline(1);
        questionService.addQuestion(question);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        questionService.updateQuestion(question);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success();
    }

    @PutMapping("/{id}/toggle")
    public Result<Void> toggle(@PathVariable Long id) {
        questionService.toggleOnline(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids != null && !ids.isEmpty()) {
            ids.forEach(questionService::deleteQuestion);
        }
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(questionService.getStats());
    }
}
