package com.quickpractice.controller;

import com.quickpractice.common.Result;
import com.quickpractice.entity.Question;
import com.quickpractice.service.GameTowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameTowerService gameTowerService;

    // ===== 闯关爬塔 =====

    @GetMapping("/tower/info")
    public Result<Map<String, Object>> towerInfo(@RequestParam String openid) {
        return Result.success(gameTowerService.getInfo(openid));
    }

    @PostMapping("/tower/start")
    public Result<List<Question>> towerStart(@RequestBody Map<String, String> body) {
        String openid = body.get("openid");
        try {
            return Result.success(gameTowerService.startFloor(openid));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/tower/submit")
    public Result<Map<String, Object>> towerSubmit(@RequestBody Map<String, Object> body) {
        String openid = (String) body.get("openid");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");
        try {
            return Result.success(gameTowerService.submitFloor(openid, answers));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // ===== 60秒竞速 =====

    @PostMapping("/speed/start")
    public Result<Map<String, Object>> speedStart(@RequestBody Map<String, String> body) {
        String openid = body.get("openid");
        try {
            return Result.success(gameTowerService.startSpeed(openid));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/speed/next")
    public Result<List<Question>> speedNext(@RequestParam String openid) {
        return Result.success(gameTowerService.nextSpeedBatch(openid));
    }

    @PostMapping("/speed/finish")
    public Result<Map<String, Object>> speedFinish(@RequestBody Map<String, Object> body) {
        String openid = (String) body.get("openid");
        int score = Integer.parseInt(body.get("score").toString());
        int correctCount = Integer.parseInt(body.get("correctCount").toString());
        int totalAnswered = Integer.parseInt(body.get("totalAnswered").toString());
        return Result.success(gameTowerService.finishSpeed(openid, score, correctCount, totalAnswered));
    }
}
