package com.quickpractice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 错题实体
 */
@Data
@TableName("wrong_question")
public class WrongQuestion implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openid;

    private Long questionId;

    private Integer wrongCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
