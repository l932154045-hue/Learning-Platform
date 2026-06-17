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
(1,  '后端开发',    0,  1),
(2,  '前端开发',    0,  2),
(3,  '数据科学',    0,  3),
(4,  '人工智能',    0,  4),
(5,  'Java',        1,  1),
(6,  'Spring Boot', 1,  2),
(7,  'Vue',         2,  1),
(8,  'React',       2,  2),
(9,  '微服务',      1,  3),
(10, '数据库',      1,  4),
(11, '运维',        1,  5),
(12, 'CSS',         2,  3),
(13, '小程序',      2,  4),
(14, 'Python',      3,  1),
(15, '大数据',      3,  2),
(16, '机器学习',    4,  1),
(17, '深度学习',    4,  2),
(18, 'AIGC',        4,  3),
(19, '移动开发',    0,  5),
(20, 'Android',     19, 1),
(21, 'Flutter',     19, 2),
(22, '测试运维',    0,  6),
(23, '自动化测试',  22, 1),
(24, '性能测试',    22, 2);

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
    'https://picsum.photos/seed/course4/400/300', 1, '赵老师', 99.00, 1560, 1),
(5, 'Linux系统管理', '全面掌握Linux系统管理的核心知识与实战技能。', 'https://picsum.photos/seed/course5/400/300', 1, '赵老师', 29.00, 3087, 1),
(6, 'Docker容器化', '全面掌握Docker容器化的核心知识与实战技能。', 'https://picsum.photos/seed/course6/400/300', 1, '吴老师', 99.00, 964, 1),
(7, 'Nginx高性能Web', '全面掌握Nginx高性能Web的核心知识与实战技能。', 'https://picsum.photos/seed/course7/400/300', 1, '刘老师', 59.00, 2821, 1),
(8, 'Java基础入门', '全面掌握Java基础入门的核心知识与实战技能。', 'https://picsum.photos/seed/course8/400/300', 5, '罗老师', 49.00, 2468, 1),
(9, 'Java面向对象编程', '全面掌握Java面向对象编程的核心知识与实战技能。', 'https://picsum.photos/seed/course9/400/300', 5, '胡老师', 39.00, 172, 1),
(10, 'Java集合框架', '全面掌握Java集合框架的核心知识与实战技能。', 'https://picsum.photos/seed/course10/400/300', 5, '王老师', 89.00, 1002, 1),
(11, 'JavaIO与网络编程', '全面掌握JavaIO与网络编程的核心知识与实战技能。', 'https://picsum.photos/seed/course11/400/300', 5, '高老师', 29.00, 2348, 1),
(12, 'JVM性能调优', '全面掌握JVM性能调优的核心知识与实战技能。', 'https://picsum.photos/seed/course12/400/300', 5, '杨老师', 299.00, 1768, 1),
(13, 'Java设计模式', '全面掌握Java设计模式的核心知识与实战技能。', 'https://picsum.photos/seed/course13/400/300', 5, '周老师', 229.00, 2463, 1),
(14, 'Maven与Gradle', '全面掌握Maven与Gradle的核心知识与实战技能。', 'https://picsum.photos/seed/course14/400/300', 5, '吴老师', 29.00, 3158, 1),
(15, 'Java21新特性', '全面掌握Java21新特性的核心知识与实战技能。', 'https://picsum.photos/seed/course15/400/300', 5, '陈老师', 199.00, 1443, 1),
(16, 'Java并发编程', '全面掌握Java并发编程的核心知识与实战技能。', 'https://picsum.photos/seed/course16/400/300', 5, '吴老师', 69.00, 931, 1),
(17, 'Spring框架入门', '全面掌握Spring框架入门的核心知识与实战技能。', 'https://picsum.photos/seed/course17/400/300', 6, '孙老师', 59.00, 429, 1),
(18, 'SpringBoot入门到精通', '全面掌握SpringBoot入门到精通的核心知识与实战技能。', 'https://picsum.photos/seed/course18/400/300', 6, '朱老师', 59.00, 1520, 1),
(19, 'SpringSecurity安全', '全面掌握SpringSecurity安全的核心知识与实战技能。', 'https://picsum.photos/seed/course19/400/300', 6, '马老师', 119.00, 3355, 1),
(20, 'SpringCloud微服务', '全面掌握SpringCloud微服务的核心知识与实战技能。', 'https://picsum.photos/seed/course20/400/300', 6, '李老师', 229.00, 2246, 1),
(21, 'SpringDataJPA实战', '全面掌握SpringDataJPA实战的核心知识与实战技能。', 'https://picsum.photos/seed/course21/400/300', 6, '赵老师', 179.00, 372, 1),
(22, 'SpringAOP切面', '全面掌握SpringAOP切面的核心知识与实战技能。', 'https://picsum.photos/seed/course22/400/300', 6, '罗老师', 129.00, 3447, 1),
(23, 'SpringMVC原理', '全面掌握SpringMVC原理的核心知识与实战技能。', 'https://picsum.photos/seed/course23/400/300', 6, '梁老师', 159.00, 2414, 1),
(24, 'DockerCompose实战', '全面掌握DockerCompose实战的核心知识与实战技能。', 'https://picsum.photos/seed/course24/400/300', 9, '杨老师', 49.00, 237, 1),
(25, '服务网格Istio', '全面掌握服务网格Istio的核心知识与实战技能。', 'https://picsum.photos/seed/course25/400/300', 9, '周老师', 129.00, 376, 1),
(26, 'API网关设计', '全面掌握API网关设计的核心知识与实战技能。', 'https://picsum.photos/seed/course26/400/300', 9, '周老师', 59.00, 1606, 1),
(27, '分布式事务实战', '全面掌握分布式事务实战的核心知识与实战技能。', 'https://picsum.photos/seed/course27/400/300', 9, '吴老师', 229.00, 2653, 1),
(28, 'MySQL性能优化', '全面掌握MySQL性能优化的核心知识与实战技能。', 'https://picsum.photos/seed/course28/400/300', 10, '马老师', 79.00, 1566, 1),
(29, 'PostgreSQL从入门到精通', '全面掌握PostgreSQL从入门到精通的核心知识与实战技能。', 'https://picsum.photos/seed/course29/400/300', 10, '马老师', 89.00, 2795, 1),
(30, 'Elasticsearch实战', '全面掌握Elasticsearch实战的核心知识与实战技能。', 'https://picsum.photos/seed/course30/400/300', 10, '吴老师', 49.00, 2545, 1),
(31, 'MongoDB实战', '全面掌握MongoDB实战的核心知识与实战技能。', 'https://picsum.photos/seed/course31/400/300', 10, '陈老师', 299.00, 3036, 1),
(32, 'Redis集群与哨兵', '全面掌握Redis集群与哨兵的核心知识与实战技能。', 'https://picsum.photos/seed/course32/400/300', 10, '周老师', 79.00, 1943, 1),
(33, 'Linux运维实战', '全面掌握Linux运维实战的核心知识与实战技能。', 'https://picsum.photos/seed/course33/400/300', 11, '朱老师', 119.00, 2671, 1),
(34, 'Prometheus监控', '全面掌握Prometheus监控的核心知识与实战技能。', 'https://picsum.photos/seed/course34/400/300', 11, '罗老师', 99.00, 2854, 1),
(35, '持续集成CICD', '全面掌握持续集成CICD的核心知识与实战技能。', 'https://picsum.photos/seed/course35/400/300', 11, '孙老师', 39.00, 988, 1),
(36, 'Web全栈入门', '全面掌握Web全栈入门的核心知识与实战技能。', 'https://picsum.photos/seed/course36/400/300', 2, '李老师', 149.00, 1693, 1),
(37, '前端工程化实践', '全面掌握前端工程化实践的核心知识与实战技能。', 'https://picsum.photos/seed/course37/400/300', 2, '吴老师', 49.00, 914, 1),
(38, 'VueRouter路由', '全面掌握VueRouter路由的核心知识与实战技能。', 'https://picsum.photos/seed/course38/400/300', 7, '郭老师', 149.00, 920, 1),
(39, 'Pinia状态管理', '全面掌握Pinia状态管理的核心知识与实战技能。', 'https://picsum.photos/seed/course39/400/300', 7, '何老师', 179.00, 2683, 1),
(40, 'Vue3响应式原理', '全面掌握Vue3响应式原理的核心知识与实战技能。', 'https://picsum.photos/seed/course40/400/300', 7, '林老师', 69.00, 1134, 1),
(41, 'Vite构建工具', '全面掌握Vite构建工具的核心知识与实战技能。', 'https://picsum.photos/seed/course41/400/300', 7, '刘老师', 99.00, 3101, 1),
(42, 'Vue组件库开发', '全面掌握Vue组件库开发的核心知识与实战技能。', 'https://picsum.photos/seed/course42/400/300', 7, '罗老师', 299.00, 1126, 1),
(43, 'VueSSR服务端渲染', '全面掌握VueSSR服务端渲染的核心知识与实战技能。', 'https://picsum.photos/seed/course43/400/300', 7, '郭老师', 199.00, 2440, 1),
(44, 'Nuxt3全栈实战', '全面掌握Nuxt3全栈实战的核心知识与实战技能。', 'https://picsum.photos/seed/course44/400/300', 7, '朱老师', 159.00, 948, 1),
(45, 'React入门到精通', '全面掌握React入门到精通的核心知识与实战技能。', 'https://picsum.photos/seed/course45/400/300', 8, '刘老师', 269.00, 2071, 1),
(46, 'ReactHooks实战', '全面掌握ReactHooks实战的核心知识与实战技能。', 'https://picsum.photos/seed/course46/400/300', 8, '王老师', 39.00, 499, 1),
(47, 'Redux状态管理', '全面掌握Redux状态管理的核心知识与实战技能。', 'https://picsum.photos/seed/course47/400/300', 8, '刘老师', 79.00, 3294, 1),
(48, 'Next.js全栈', '全面掌握Next.js全栈的核心知识与实战技能。', 'https://picsum.photos/seed/course48/400/300', 8, '胡老师', 49.00, 1626, 1),
(49, 'ReactNative入门', '全面掌握ReactNative入门的核心知识与实战技能。', 'https://picsum.photos/seed/course49/400/300', 8, '朱老师', 229.00, 2217, 1),
(50, 'AntDesign实战', '全面掌握AntDesign实战的核心知识与实战技能。', 'https://picsum.photos/seed/course50/400/300', 8, '吴老师', 299.00, 97, 1),
(51, 'CSS布局大师', '全面掌握CSS布局大师的核心知识与实战技能。', 'https://picsum.photos/seed/course51/400/300', 12, '赵老师', 299.00, 3125, 1),
(52, 'TailwindCSS实战', '全面掌握TailwindCSS实战的核心知识与实战技能。', 'https://picsum.photos/seed/course52/400/300', 12, '吴老师', 149.00, 506, 1),
(53, 'SCSS高级技巧', '全面掌握SCSS高级技巧的核心知识与实战技能。', 'https://picsum.photos/seed/course53/400/300', 12, '郑老师', 199.00, 697, 1),
(54, '微信小程序入门', '全面掌握微信小程序入门的核心知识与实战技能。', 'https://picsum.photos/seed/course54/400/300', 13, '林老师', 29.00, 3007, 1),
(55, 'Uni-App跨平台', '全面掌握Uni-App跨平台的核心知识与实战技能。', 'https://picsum.photos/seed/course55/400/300', 13, '吴老师', 269.00, 3170, 1),
(56, 'Taro多端开发', '全面掌握Taro多端开发的核心知识与实战技能。', 'https://picsum.photos/seed/course56/400/300', 13, '陈老师', 269.00, 485, 1),
(57, 'Python数据分析', '全面掌握Python数据分析的核心知识与实战技能。', 'https://picsum.photos/seed/course57/400/300', 3, '郑老师', 269.00, 2544, 1),
(58, '数据科学导论', '全面掌握数据科学导论的核心知识与实战技能。', 'https://picsum.photos/seed/course58/400/300', 3, '杨老师', 69.00, 1581, 1),
(59, 'Python基础入门', '全面掌握Python基础入门的核心知识与实战技能。', 'https://picsum.photos/seed/course59/400/300', 14, '陈老师', 299.00, 3239, 1),
(60, 'Pandas数据分析', '全面掌握Pandas数据分析的核心知识与实战技能。', 'https://picsum.photos/seed/course60/400/300', 14, '高老师', 29.00, 2503, 1),
(61, 'NumPy科学计算', '全面掌握NumPy科学计算的核心知识与实战技能。', 'https://picsum.photos/seed/course61/400/300', 14, '孙老师', 249.00, 129, 1),
(62, 'Scikit-learn实战', '全面掌握Scikit-learn实战的核心知识与实战技能。', 'https://picsum.photos/seed/course62/400/300', 14, '赵老师', 159.00, 3456, 1),
(63, 'Python爬虫实战', '全面掌握Python爬虫实战的核心知识与实战技能。', 'https://picsum.photos/seed/course63/400/300', 14, '郑老师', 99.00, 287, 1),
(64, 'Python自动化运维', '全面掌握Python自动化运维的核心知识与实战技能。', 'https://picsum.photos/seed/course64/400/300', 14, '周老师', 49.00, 400, 1),
(65, 'Spark大数据处理', '全面掌握Spark大数据处理的核心知识与实战技能。', 'https://picsum.photos/seed/course65/400/300', 15, '何老师', 49.00, 3165, 1),
(66, 'Flink实时计算', '全面掌握Flink实时计算的核心知识与实战技能。', 'https://picsum.photos/seed/course66/400/300', 15, '罗老师', 69.00, 575, 1),
(67, '数据仓库建模', '全面掌握数据仓库建模的核心知识与实战技能。', 'https://picsum.photos/seed/course67/400/300', 15, '何老师', 299.00, 726, 1),
(68, 'ETL数据清洗', '全面掌握ETL数据清洗的核心知识与实战技能。', 'https://picsum.photos/seed/course68/400/300', 15, '吴老师', 269.00, 2534, 1),
(69, 'Hadoop生态实战', '全面掌握Hadoop生态实战的核心知识与实战技能。', 'https://picsum.photos/seed/course69/400/300', 15, '胡老师', 89.00, 2258, 1),
(70, '人工智能导论', '全面掌握人工智能导论的核心知识与实战技能。', 'https://picsum.photos/seed/course70/400/300', 4, '杨老师', 129.00, 1684, 1),
(71, 'AI伦理与安全', '全面掌握AI伦理与安全的核心知识与实战技能。', 'https://picsum.photos/seed/course71/400/300', 4, '马老师', 229.00, 2169, 1),
(72, '机器学习入门', '全面掌握机器学习入门的核心知识与实战技能。', 'https://picsum.photos/seed/course72/400/300', 16, '林老师', 59.00, 1065, 1),
(73, '特征工程实战', '全面掌握特征工程实战的核心知识与实战技能。', 'https://picsum.photos/seed/course73/400/300', 16, '周老师', 49.00, 1434, 1),
(74, '集成学习算法', '全面掌握集成学习算法的核心知识与实战技能。', 'https://picsum.photos/seed/course74/400/300', 16, '张老师', 299.00, 992, 1),
(75, '推荐系统实战', '全面掌握推荐系统实战的核心知识与实战技能。', 'https://picsum.photos/seed/course75/400/300', 16, '郭老师', 99.00, 79, 1),
(76, '时间序列预测', '全面掌握时间序列预测的核心知识与实战技能。', 'https://picsum.photos/seed/course76/400/300', 16, '王老师', 39.00, 987, 1),
(77, '深度学习与神经网络', '全面掌握深度学习与神经网络的核心知识与实战技能。', 'https://picsum.photos/seed/course77/400/300', 17, '王老师', 39.00, 1403, 1),
(78, 'PyTorch框架实战', '全面掌握PyTorch框架实战的核心知识与实战技能。', 'https://picsum.photos/seed/course78/400/300', 17, '王老师', 269.00, 1024, 1),
(79, '计算机视觉CV', '全面掌握计算机视觉CV的核心知识与实战技能。', 'https://picsum.photos/seed/course79/400/300', 17, '吴老师', 249.00, 927, 1),
(80, 'Transformer架构', '全面掌握Transformer架构的核心知识与实战技能。', 'https://picsum.photos/seed/course80/400/300', 17, '罗老师', 69.00, 3012, 1),
(81, '大模型应用开发', '全面掌握大模型应用开发的核心知识与实战技能。', 'https://picsum.photos/seed/course81/400/300', 18, '郭老师', 249.00, 1045, 1),
(82, 'Prompt工程实战', '全面掌握Prompt工程实战的核心知识与实战技能。', 'https://picsum.photos/seed/course82/400/300', 18, '何老师', 199.00, 829, 1),
(83, 'AI Agent开发', '全面掌握AI Agent开发的核心知识与实战技能。', 'https://picsum.photos/seed/course83/400/300', 18, '赵老师', 59.00, 2749, 1),
(84, 'LangChain实战', '全面掌握LangChain实战的核心知识与实战技能。', 'https://picsum.photos/seed/course84/400/300', 18, '胡老师', 159.00, 1784, 1),
(85, '生成式AI入门', '全面掌握生成式AI入门的核心知识与实战技能。', 'https://picsum.photos/seed/course85/400/300', 18, '胡老师', 229.00, 3036, 1),
(86, '移动开发入门', '全面掌握移动开发入门的核心知识与实战技能。', 'https://picsum.photos/seed/course86/400/300', 19, '李老师', 59.00, 298, 1),
(87, '跨平台方案选型', '全面掌握跨平台方案选型的核心知识与实战技能。', 'https://picsum.photos/seed/course87/400/300', 19, '朱老师', 149.00, 3329, 1),
(88, 'Android基础开发', '全面掌握Android基础开发的核心知识与实战技能。', 'https://picsum.photos/seed/course88/400/300', 20, '赵老师', 99.00, 834, 1),
(89, 'Kotlin协程实战', '全面掌握Kotlin协程实战的核心知识与实战技能。', 'https://picsum.photos/seed/course89/400/300', 20, '杨老师', 299.00, 1887, 1),
(90, 'Jetpack架构组件', '全面掌握Jetpack架构组件的核心知识与实战技能。', 'https://picsum.photos/seed/course90/400/300', 20, '刘老师', 199.00, 801, 1),
(91, 'Android性能优化', '全面掌握Android性能优化的核心知识与实战技能。', 'https://picsum.photos/seed/course91/400/300', 20, '吴老师', 229.00, 1073, 1),
(92, 'Flutter入门到精通', '全面掌握Flutter入门到精通的核心知识与实战技能。', 'https://picsum.photos/seed/course92/400/300', 21, '王老师', 229.00, 3359, 1),
(93, 'Dart语言实战', '全面掌握Dart语言实战的核心知识与实战技能。', 'https://picsum.photos/seed/course93/400/300', 21, '罗老师', 59.00, 257, 1),
(94, 'Flutter状态管理', '全面掌握Flutter状态管理的核心知识与实战技能。', 'https://picsum.photos/seed/course94/400/300', 21, '罗老师', 29.00, 432, 1),
(95, 'Flutter动画实战', '全面掌握Flutter动画实战的核心知识与实战技能。', 'https://picsum.photos/seed/course95/400/300', 21, '周老师', 79.00, 1714, 1),
(96, '软件测试基础', '全面掌握软件测试基础的核心知识与实战技能。', 'https://picsum.photos/seed/course96/400/300', 22, '何老师', 249.00, 925, 1),
(97, 'DevOps实践', '全面掌握DevOps实践的核心知识与实战技能。', 'https://picsum.photos/seed/course97/400/300', 22, '朱老师', 39.00, 724, 1),
(98, 'Selenium自动化测试', '全面掌握Selenium自动化测试的核心知识与实战技能。', 'https://picsum.photos/seed/course98/400/300', 23, '朱老师', 29.00, 1649, 1),
(99, '接口自动化测试', '全面掌握接口自动化测试的核心知识与实战技能。', 'https://picsum.photos/seed/course99/400/300', 23, '吴老师', 229.00, 1218, 1),
(100, 'Appium移动测试', '全面掌握Appium移动测试的核心知识与实战技能。', 'https://picsum.photos/seed/course100/400/300', 23, '胡老师', 299.00, 2761, 1),
(101, '端到端测试Cypress', '全面掌握端到端测试Cypress的核心知识与实战技能。', 'https://picsum.photos/seed/course101/400/300', 23, '何老师', 69.00, 827, 1),
(102, 'JMeter性能测试', '全面掌握JMeter性能测试的核心知识与实战技能。', 'https://picsum.photos/seed/course102/400/300', 24, '郑老师', 89.00, 289, 1),
(103, '压力测试实战', '全面掌握压力测试实战的核心知识与实战技能。', 'https://picsum.photos/seed/course103/400/300', 24, '郭老师', 299.00, 299, 1),
(104, '全链路压测', '全面掌握全链路压测的核心知识与实战技能。', 'https://picsum.photos/seed/course104/400/300', 24, '孙老师', 39.00, 255, 1);

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
