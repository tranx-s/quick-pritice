package com.quickpractice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickpractice.entity.GameTower;
import com.quickpractice.entity.Question;
import com.quickpractice.mapper.GameTowerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GameTowerService {

    private final GameTowerMapper gameTowerMapper;
    private final QuestionService questionService;
    private final WrongQuestionService wrongQuestionService;
    private final UserService userService;

    private static final int QUESTIONS_PER_FLOOR = 8;
    private static final int PASS_THRESHOLD = 6;
    private static final int DAILY_LIVES = 5;

    public GameTower getOrCreate(String openid) {
        LambdaQueryWrapper<GameTower> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GameTower::getOpenid, openid);
        GameTower tower = gameTowerMapper.selectOne(wrapper);
        if (tower == null) {
            tower = new GameTower();
            tower.setOpenid(openid);
            tower.setCurrentFloor(1);
            tower.setTodayLife(DAILY_LIVES);
            tower.setTodayScore(0);
            tower.setMaxFloor(1);
            tower.setTodaySpeedScore(0);
            tower.setSpeedCount(3);
            gameTowerMapper.insert(tower);
        }
        return tower;
    }

    public List<Question> startFloor(String openid) {
        GameTower tower = getOrCreate(openid);
        if (tower.getTodayLife() <= 0) {
            throw new RuntimeException("今日生命值已耗尽，明日再来");
        }
        List<Question> questions = questionService.getRandomQuestions(QUESTIONS_PER_FLOOR, null);
        return questions;
    }

    public Map<String, Object> submitFloor(String openid, List<Map<String, Object>> answers) {
        GameTower tower = getOrCreate(openid);
        if (tower.getTodayLife() <= 0) {
            throw new RuntimeException("今日生命值已耗尽");
        }

        int correctCount = 0;
        for (Map<String, Object> ans : answers) {
            Long questionId = Long.valueOf(ans.get("questionId").toString());
            String userAnswer = (String) ans.get("userAnswer");
            Question q = questionService.getById(questionId);
            if (q != null && q.getCorrectAnswer().equals(userAnswer)) {
                correctCount++;
            } else if (q != null) {
                wrongQuestionService.addWrongQuestion(openid, questionId);
            }
        }

        boolean passed = correctCount >= PASS_THRESHOLD;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("correctCount", correctCount);
        result.put("total", QUESTIONS_PER_FLOOR);
        result.put("passed", passed);
        result.put("passThreshold", PASS_THRESHOLD);

        if (passed) {
            tower.setCurrentFloor(tower.getCurrentFloor() + 1);
            if (tower.getCurrentFloor() > tower.getMaxFloor()) {
                tower.setMaxFloor(tower.getCurrentFloor());
            }
            result.put("message", "通关！进入第 " + tower.getCurrentFloor() + " 层");
        } else {
            tower.setTodayLife(tower.getTodayLife() - 1);
            result.put("message", "未通关，答对 " + correctCount + "/" + QUESTIONS_PER_FLOOR + " 题，剩余生命 " + tower.getTodayLife());
        }

        result.put("currentFloor", tower.getCurrentFloor());
        result.put("todayLife", tower.getTodayLife());
        result.put("maxFloor", tower.getMaxFloor());

        gameTowerMapper.updateById(tower);
        userService.increaseQuestionCount(openid, answers.size());

        return result;
    }

    public Map<String, Object> getInfo(String openid) {
        GameTower tower = getOrCreate(openid);
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("currentFloor", tower.getCurrentFloor());
        info.put("todayLife", tower.getTodayLife());
        info.put("maxFloor", tower.getMaxFloor());
        info.put("todaySpeedScore", tower.getTodaySpeedScore());
        info.put("speedCount", tower.getSpeedCount());
        info.put("passThreshold", PASS_THRESHOLD);
        info.put("questionsPerFloor", QUESTIONS_PER_FLOOR);
        return info;
    }

    public Map<String, Object> startSpeed(String openid) {
        GameTower tower = getOrCreate(openid);
        if (tower.getSpeedCount() <= 0) {
            throw new RuntimeException("今日竞速次数已用完");
        }
        tower.setSpeedCount(tower.getSpeedCount() - 1);
        gameTowerMapper.updateById(tower);

        List<Question> questions = questionService.getRandomQuestions(10, null);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("questions", questions);
        result.put("speedCount", tower.getSpeedCount());
        result.put("todayBest", tower.getTodaySpeedScore());
        return result;
    }

    public List<Question> nextSpeedBatch(String openid) {
        return questionService.getRandomQuestions(10, null);
    }

    public Map<String, Object> finishSpeed(String openid, int score, int correctCount, int totalAnswered) {
        GameTower tower = getOrCreate(openid);
        boolean isNewBest = score > tower.getTodaySpeedScore();
        if (isNewBest) {
            tower.setTodaySpeedScore(score);
            gameTowerMapper.updateById(tower);
        }
        userService.increaseQuestionCount(openid, totalAnswered);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", score);
        result.put("correctCount", correctCount);
        result.put("totalAnswered", totalAnswered);
        result.put("isNewBest", isNewBest);
        result.put("todayBest", tower.getTodaySpeedScore());
        return result;
    }
}
