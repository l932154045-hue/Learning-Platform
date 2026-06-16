-- ============================================================
-- 在线课程平台 - 数据库初始化脚本
-- 使用方法: 在 DataGrip 中连接到 192.168.100.128:3306
--          用户名 root，密码 040615，然后执行此脚本
-- ============================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS learning_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_course DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_cart DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_payment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_learning DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- 2. learning_user 库 - 用户表
-- ============================================================
USE learning_user;

CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`   VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`   VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    `nickname`   VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `email`      VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone`      VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role`       INT          NOT NULL DEFAULT 0 COMMENT '角色: 0=普通用户, 1=管理员',
    `status`     INT          NOT NULL DEFAULT 1 COMMENT '状态: 1=正常, 0=禁用',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入默认用户（密码 123456 的 BCrypt 哈希，手机号用于登录）
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `phone`, `role`, `status`) VALUES
('admin',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员',   'admin@learning.com',   '13800000001', 1, 1),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三',    'zhangsan@example.com', '13800000002', 0, 1),
('lisi',     '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四',    'lisi@example.com',     '13800000003', 0, 1),
('wangwu',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五',    'wangwu@example.com',   '13800000004', 0, 1);

-- ============================================================
-- 3. learning_course 库 - 课程相关表
-- ============================================================
USE learning_course;

-- 课程分类表
CREATE TABLE IF NOT EXISTS `course_category` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`       VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id`  BIGINT      NOT NULL DEFAULT 0 COMMENT '父分类ID，0=顶级',
    `sort_order` INT         NOT NULL DEFAULT 0 COMMENT '排序序号',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程分类表';

-- 课程表
CREATE TABLE IF NOT EXISTS `course` (
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '课程ID',
    `title`        VARCHAR(200)   NOT NULL COMMENT '课程标题',
    `description`  TEXT           DEFAULT NULL COMMENT '课程简介',
    `cover_url`    VARCHAR(255)   DEFAULT NULL COMMENT '封面图URL',
    `category_id`  BIGINT         NOT NULL COMMENT '分类ID',
    `teacher_name` VARCHAR(50)    DEFAULT NULL COMMENT '讲师姓名',
    `price`        DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '价格',
    `sale_count`   INT            NOT NULL DEFAULT 0 COMMENT '销量',
    `status`       INT            NOT NULL DEFAULT 1 COMMENT '状态: 1=上架, 0=下架',
    `created_at`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 章节视频表
CREATE TABLE IF NOT EXISTS `chapter_video` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '视频ID',
    `course_id`     BIGINT       NOT NULL COMMENT '所属课程ID',
    `chapter_title` VARCHAR(200) NOT NULL COMMENT '章标题',
    `video_title`   VARCHAR(200) NOT NULL COMMENT '视频标题',
    `video_url`     VARCHAR(500) DEFAULT NULL COMMENT '视频URL',
    `duration`      INT          DEFAULT 0 COMMENT '视频时长(秒)',
    `sort_order`    INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    `status`        INT          NOT NULL DEFAULT 1 COMMENT '状态: 1=正常, 0=禁用',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节视频表';

-- 插入分类数据
INSERT INTO `course_category` (`id`, `name`, `parent_id`, `sort_order`) VALUES
(1, '后端开发',    0, 1),
(2, '前端开发',    0, 2),
(3, '数据科学',    0, 3),
(4, '人工智能',    0, 4),
(5, 'Java',        1, 1),
(6, 'Spring Boot', 1, 2),
(7, 'Vue',         2, 1),
(8, 'React',       2, 2);

-- 插入课程数据
INSERT INTO `course` (`id`, `title`, `description`, `cover_url`, `category_id`, `teacher_name`, `price`, `sale_count`, `status`) VALUES
(1, 'Spring Boot 3 微服务实战',
    '从零开始构建 Spring Boot 3 微服务项目，涵盖 Spring Cloud、MyBatis Plus、RabbitMQ、Redis 等技术栈。',
    'https://picsum.photos/seed/course1/400/300', 6, '张老师', 199.00, 1280, 1),
(2, 'Vue 3 + Element Plus 企业级后台',
    '使用 Vue 3 Composition API + Element Plus 从零搭建企业级后台管理系统。',
    'https://picsum.photos/seed/course2/400/300', 7, '李老师', 159.00, 960, 1),
(3, 'Java 并发编程深入浅出',
    '深入理解 Java 并发编程，掌握多线程、JUC、锁机制等核心知识。',
    'https://picsum.photos/seed/course3/400/300', 5, '王老师', 129.00, 2100, 1),
(4, 'Redis 核心原理与实战',
    '全面掌握 Redis 核心数据结构、持久化、集群、缓存策略与实战案例。',
    'https://picsum.photos/seed/course4/400/300', 1, '赵老师', 99.00, 1560, 1);

-- 插入章节视频数据
INSERT INTO `chapter_video` (`course_id`, `chapter_title`, `video_title`, `video_url`, `duration`, `sort_order`) VALUES
-- Spring Boot 课程
(1, '第一章：项目概述',   '课程介绍与前置准备',         'https://example.com/video/1-1.mp4', 720,  1),
(1, '第一章：项目概述',   '微服务架构设计',             'https://example.com/video/1-2.mp4', 900,  2),
(1, '第二章：环境搭建',   '开发环境配置',               'https://example.com/video/2-1.mp4', 600,  3),
(1, '第二章：环境搭建',   '项目骨架搭建',               'https://example.com/video/2-2.mp4', 1080, 4),
(1, '第三章：用户服务',   '用户注册与登录',             'https://example.com/video/3-1.mp4', 1200, 5),
-- Vue 课程
(2, '第一章：Vue3 基础', 'Composition API 入门',       'https://example.com/video/v3-1.mp4', 900,  1),
(2, '第一章：Vue3 基础', '响应式数据与计算属性',       'https://example.com/video/v3-2.mp4', 780,  2),
(2, '第二章：组件开发',  'Element Plus 组件库使用',    'https://example.com/video/v3-3.mp4', 960,  3),
-- Java 并发课程
(3, '第一章：线程基础',  '线程的创建与生命周期',       'https://example.com/video/j1-1.mp4', 840,  1),
(3, '第一章：线程基础',  'synchronized 关键字深度解析','https://example.com/video/j1-2.mp4', 1020, 2),
(3, '第二章：JUC 包',   'AQS 原理与 ReentrantLock',   'https://example.com/video/j2-1.mp4', 1200, 3),
-- Redis 课程
(4, '第一章：数据结构',  'String 与 Hash 的应用场景',  'https://example.com/video/r1-1.mp4', 660,  1),
(4, '第一章：数据结构',  'List、Set 与 ZSet 实战',     'https://example.com/video/r1-2.mp4', 840,  2),
(4, '第二章：高级特性',  'Redis 持久化 RDB 与 AOF',    'https://example.com/video/r2-1.mp4', 1080, 3);

-- ============================================================
-- 4. learning_cart 库 - 购物车表
-- ============================================================
USE learning_cart;

CREATE TABLE IF NOT EXISTS `cart` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `course_id`  BIGINT   NOT NULL COMMENT '课程ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ============================================================
-- 5. learning_order 库 - 订单表
-- ============================================================
USE learning_order;

CREATE TABLE IF NOT EXISTS `order` (
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`     VARCHAR(32)    NOT NULL COMMENT '订单编号',
    `user_id`      BIGINT         NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '订单总额',
    `status`       INT            NOT NULL DEFAULT 1 COMMENT '状态: 1=待支付, 2=已支付, 3=已取消, 4=已退款',
    `paid_at`      DATETIME       DEFAULT NULL COMMENT '支付时间',
    `created_at`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `order_item` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
    `order_id`     BIGINT        NOT NULL COMMENT '订单ID',
    `course_id`    BIGINT        NOT NULL COMMENT '课程ID',
    `course_title` VARCHAR(200)  NOT NULL COMMENT '课程标题(快照)',
    `price`        DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价(快照)',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';

-- ============================================================
-- 6. learning_payment 库 - 支付表
-- ============================================================
USE learning_payment;

CREATE TABLE IF NOT EXISTS `payment_record` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
    `payment_no`  VARCHAR(32)    NOT NULL COMMENT '支付编号',
    `order_id`    BIGINT         NOT NULL COMMENT '订单ID',
    `order_no`    VARCHAR(32)    NOT NULL COMMENT '订单编号',
    `user_id`     BIGINT         NOT NULL COMMENT '用户ID',
    `amount`      DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '支付金额',
    `pay_method`  VARCHAR(20)    DEFAULT NULL COMMENT '支付方式: alipay/wechat/card',
    `status`      INT            NOT NULL DEFAULT 1 COMMENT '状态: 1=待支付, 2=支付成功, 3=支付失败, 4=已退款',
    `paid_at`     DATETIME       DEFAULT NULL COMMENT '支付时间',
    `created_at`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- ============================================================
-- 7. learning_learning 库 - 学习相关表
-- ============================================================
USE learning_learning;

CREATE TABLE IF NOT EXISTS `enrollment` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '选课ID',
    `user_id`         BIGINT   NOT NULL COMMENT '用户ID',
    `course_id`       BIGINT   NOT NULL COMMENT '课程ID',
    `status`          INT      NOT NULL DEFAULT 1 COMMENT '状态: 1=学习中, 2=已完成',
    `enrolled_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    `last_learned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近学习时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课表';

CREATE TABLE IF NOT EXISTS `course_review` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `user_id`    BIGINT       NOT NULL COMMENT '用户ID',
    `course_id`  BIGINT       NOT NULL COMMENT '课程ID',
    `rating`     INT          NOT NULL DEFAULT 5 COMMENT '评分: 1-5',
    `content`    TEXT         DEFAULT NULL COMMENT '评价内容',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程评价表';

CREATE TABLE IF NOT EXISTS `video_progress` (
    `id`               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '进度ID',
    `user_id`          BIGINT   NOT NULL COMMENT '用户ID',
    `video_id`         BIGINT   NOT NULL COMMENT '视频ID',
    `course_id`        BIGINT   NOT NULL COMMENT '课程ID',
    `progress_seconds` INT      NOT NULL DEFAULT 0 COMMENT '已观看秒数',
    `is_finished`      INT      NOT NULL DEFAULT 0 COMMENT '是否看完: 0=未完成, 1=已完成',
    `updated_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_video` (`user_id`, `video_id`),
    KEY `idx_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频学习进度表';
