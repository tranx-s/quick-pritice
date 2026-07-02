package com.quickpractice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 闯关游戏实体
 */
@Data
@TableName("game_tower")
public class GameTower implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openid;

    private Integer currentFloor;

    private Integer todayLife;

    private Integer todayScore;

    private Integer maxFloor;

    private Integer todaySpeedScore;

    private Integer speedCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
