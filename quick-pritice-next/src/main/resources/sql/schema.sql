-- 创建数据库
CREATE DATABASE IF NOT EXISTS quick_practice DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE quick_practice;

-- 1. 用户表
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '微信openid',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `vip_status` TINYINT DEFAULT 0 COMMENT '会员状态 0-普通 1-会员',
  `vip_expire_time` DATETIME DEFAULT NULL COMMENT '会员到期时间',
  `total_questions` INT DEFAULT 0 COMMENT '累计刷题数',
  `today_questions` INT DEFAULT 0 COMMENT '今日刷题数',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_vip_status` (`vip_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 题目表
CREATE TABLE `question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module_type` VARCHAR(32) NOT NULL COMMENT '模块类型: 言语理解/判断推理/资料分析/常识判断/数量关系',
  `content` TEXT NOT NULL COMMENT '题目内容',
  `option_a` VARCHAR(500) DEFAULT NULL COMMENT '选项A',
  `option_b` VARCHAR(500) DEFAULT NULL COMMENT '选项B',
  `option_c` VARCHAR(500) DEFAULT NULL COMMENT '选项C',
  `option_d` VARCHAR(500) DEFAULT NULL COMMENT '选项D',
  `correct_answer` CHAR(1) NOT NULL COMMENT '正确答案 A/B/C/D',
  `analysis` TEXT DEFAULT NULL COMMENT '答案解析',
  `is_online` TINYINT DEFAULT 1 COMMENT '是否上架 0-下架 1-上架',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '题目图片地址',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `source` VARCHAR(50) DEFAULT 'manual' COMMENT '题目来源: manual/fenbi/offcn/huatu/yuantiku',
  `source_id` VARCHAR(100) DEFAULT NULL COMMENT '来源网站原始ID，用于去重',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_module_type` (`module_type`),
  KEY `idx_is_online` (`is_online`),
  KEY `idx_source` (`source`),
  UNIQUE KEY `uk_source_id` (`source`, `source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 3. 错题表
CREATE TABLE `wrong_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '用户openid',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `wrong_count` INT DEFAULT 1 COMMENT '错误次数',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid_question` (`openid`, `question_id`),
  KEY `idx_openid` (`openid`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题表';

-- 4. 收藏表
CREATE TABLE `collect` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '用户openid',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid_question` (`openid`, `question_id`),
  KEY `idx_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 5. 闯关游戏表
CREATE TABLE `game_tower` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '用户openid',
  `current_floor` INT DEFAULT 1 COMMENT '当前层数',
  `today_life` INT DEFAULT 5 COMMENT '今日生命值',
  `today_score` INT DEFAULT 0 COMMENT '今日积分',
  `max_floor` INT DEFAULT 1 COMMENT '历史最高层数',
  `today_speed_score` INT DEFAULT 0 COMMENT '今日竞速分数',
  `speed_count` INT DEFAULT 3 COMMENT '今日竞速次数',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闯关游戏表';

-- 6. 排行榜表
CREATE TABLE `rank` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '用户openid',
  `score` INT NOT NULL COMMENT '分数',
  `ranking` INT DEFAULT NULL COMMENT '排名',
  `date` DATE NOT NULL COMMENT '日期',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid_date` (`openid`, `date`),
  KEY `idx_date_score` (`date`, `score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排行榜表';

-- 7. 会员订单表
CREATE TABLE `vip_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `openid` VARCHAR(64) NOT NULL COMMENT '用户openid',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `vip_type` VARCHAR(32) NOT NULL COMMENT '会员类型: 月卡/季卡/永久',
  `duration_days` INT NOT NULL COMMENT '会员时长(天)',
  `pay_status` TINYINT DEFAULT 0 COMMENT '支付状态 0-待支付 1-已支付 2-已取消',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `paid_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_openid` (`openid`),
  KEY `idx_pay_status` (`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员订单表';

-- ============================================================
-- 数据库变更：question 表新增来源字段（已有库执行此段）
-- ============================================================
ALTER TABLE `question`
  ADD COLUMN IF NOT EXISTS `source` VARCHAR(50) DEFAULT 'manual' COMMENT '题目来源: manual/fenbi/offcn/huatu/yuantiku' AFTER `image_url`,
  ADD COLUMN IF NOT EXISTS `source_id` VARCHAR(100) DEFAULT NULL COMMENT '来源网站原始ID，用于去重' AFTER `source`;

-- 为已有数据补全 source 默认值
UPDATE `question` SET `source` = 'manual' WHERE `source` IS NULL;

-- 创建联合唯一索引（去重用，忽略 null source_id）
ALTER TABLE `question`
  ADD KEY IF NOT EXISTS `idx_source` (`source`),
  ADD UNIQUE KEY IF NOT EXISTS `uk_source_id` (`source`, `source_id`);
