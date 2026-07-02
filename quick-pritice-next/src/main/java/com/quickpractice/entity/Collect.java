package com.quickpractice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏实体
 */
@Data
@TableName("collect")
public class Collect implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openid;

    private Long questionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableLogic
    private Integer deleted;
}
