/*
 Navicat Premium Dump SQL

 Source Server         : MyDatabase
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : laboratory

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 24/05/2026 19:02:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `admin_id` int NOT NULL AUTO_INCREMENT COMMENT '管理员ID，自增主键',
  `account` int NOT NULL COMMENT '管理员账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员密码',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '正常' COMMENT '状态：正常、禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，自动填充',
  PRIMARY KEY (`admin_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 111, '$2a$10$8Zx.kRmPo42ft8DnMDeYfu1ziT0xE.pl9/2mCHmH3j6Cy9Gmivl8G', '正常', '2026-04-09 15:42:17');

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `announcement_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `image_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公告图片URL',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `publish_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `is_top` tinyint(1) NULL DEFAULT NULL,
  PRIMARY KEY (`announcement_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcement
-- ----------------------------

-- ----------------------------
-- Table structure for device
-- ----------------------------
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device`  (
  `device_id` int NOT NULL AUTO_INCREMENT,
  `device_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备名称',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备分类',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备型号',
  `purchase_date` date NULL DEFAULT NULL COMMENT '购买时间',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '购买价格',
  `laboratory_id` int NULL DEFAULT NULL COMMENT '实验室ID（放在哪个实验室）',
  `image_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片',
  `spec` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '规格参数',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '设备描述',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备状态',
  `required_permission` int NULL DEFAULT NULL COMMENT '权限ID',
  `manager_id` int NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建该数据时间',
  PRIMARY KEY (`device_id`) USING BTREE,
  INDEX `idx_device_status`(`status` ASC) USING BTREE,
  INDEX `idx_device_lab`(`laboratory_id` ASC) USING BTREE,
  INDEX `idx_device_manager`(`manager_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device
-- ----------------------------
INSERT INTO `device` VALUES (1, '紫外分光光度计', '光谱分析仪器', 'UV-2600', '2026-03-05', 58000.00, 3, '紫外分光光度计.png', '波长范围：190-1100nm，带宽：2nm', '用于物质定量分析，测量样品吸光度', '闲置', 1, 2, '2026-03-19 20:31:08');
INSERT INTO `device` VALUES (2, '电子天平', '称量设备', 'FA2004N', '2023-08-20', 4500.00, 1, '电子天平.png', '量程：0-200g，精度：0.0001g', '高精度电子分析天平，用于精密称量', '闲置', 1, 2, '2026-03-29 23:00:08');
INSERT INTO `device` VALUES (3, '离心机', '分离设备', 'TGL-16M', '2023-10-10', 6800.00, 1, '离心机.png', '最高转速：16000rpm，最大离心力：17800×g', '高速冷冻离心机，用于样品分离', '闲置', 2, 2, '2026-03-29 23:00:08');
INSERT INTO `device` VALUES (4, 'pH计', '测量仪器', 'PHS-3C', '2024-01-05', 1200.00, 1, 'pH计.png', '测量范围：0-14pH，精度：±0.01pH', '实验室pH酸碱度测量仪', '闲置', 1, 2, '2026-03-29 23:00:08');
INSERT INTO `device` VALUES (5, '磁力搅拌器', '搅拌设备', '85-2', '2023-12-12', 800.00, 1, '磁力搅拌器.png', '搅拌容量：20-3000ml，转速：0-2600rpm', '恒温磁力搅拌器，用于溶液混合', '闲置', 1, 3, '2026-03-29 23:00:08');
INSERT INTO `device` VALUES (6, '光学显微镜', '观察设备', 'BX53', '2022-09-18', 35000.00, 2, '光学显微镜.png', '放大倍数：40-1000倍，配拍照系统', '研究级生物显微镜，用于细胞观察', '闲置', 3, 3, '2026-03-29 23:00:08');
INSERT INTO `device` VALUES (7, '示波器', '电子仪器', 'DS1104Z', '2023-11-22', 5200.00, 2, '示波器.png', '带宽：100MHz，采样率：1GSa/s', '四通道数字存储示波器', '闲置', 2, 3, '2026-03-29 23:00:08');
INSERT INTO `device` VALUES (8, '函数信号发生器', '电子仪器', 'DG1022', '2024-02-28', 2800.00, 2, '函数信号发生器.png', '频率范围：0-25MHz，输出波形：正弦、方波、三角波', '任意波形信号发生器', '闲置', 2, 4, '2026-03-29 23:00:08');
INSERT INTO `device` VALUES (9, '生物安全柜', '安全设备', 'BSC-1304IIA2', '2022-05-16', 42000.00, 3, '生物安全柜.png', '洁净等级：ISO5级，风速：0.3-0.5m/s', '二级生物安全柜，用于微生物操作', '闲置', 3, 4, '2026-03-29 23:00:08');
INSERT INTO `device` VALUES (10, '恒温培养箱', '培养设备', 'DHP-9052', '2023-07-30', 5600.00, 3, '恒温培养箱.png', '控温范围：RT+5-65℃，温度波动：±0.5℃', '电热恒温培养箱，用于微生物培养', '闲置', 2, 4, '2026-03-29 23:00:08');

-- ----------------------------
-- Table structure for fault_report
-- ----------------------------
DROP TABLE IF EXISTS `fault_report`;
CREATE TABLE `fault_report`  (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `reservation_id` int NULL DEFAULT NULL,
  `user_id` int NULL DEFAULT NULL,
  `device_id` int NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`report_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fault_report
-- ----------------------------

-- ----------------------------
-- Table structure for fault_report_lab
-- ----------------------------
DROP TABLE IF EXISTS `fault_report_lab`;
CREATE TABLE `fault_report_lab`  (
  `report_id` int NOT NULL AUTO_INCREMENT COMMENT '故障报告ID',
  `lab_reservation_id` int NOT NULL COMMENT '预约ID',
  `user_id` int NULL DEFAULT NULL COMMENT '报告人ID',
  `laboratory_id` int NOT NULL COMMENT '实验室ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '故障描述',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '故障图片（JSON格式存储图片路径）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态：待处理、处理中、已处理、已忽略',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`report_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '实验室故障报告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fault_report_lab
-- ----------------------------

-- ----------------------------
-- Table structure for global_disabled_period
-- ----------------------------
DROP TABLE IF EXISTS `global_disabled_period`;
CREATE TABLE `global_disabled_period`  (
  `disable_id` int NOT NULL AUTO_INCREMENT,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int NULL DEFAULT NULL,
  PRIMARY KEY (`disable_id`) USING BTREE,
  INDEX `idx_time`(`start_time` ASC, `end_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of global_disabled_period
-- ----------------------------

-- ----------------------------
-- Table structure for lab_reservation
-- ----------------------------
DROP TABLE IF EXISTS `lab_reservation`;
CREATE TABLE `lab_reservation`  (
  `lab_reservation_id` int NOT NULL AUTO_INCREMENT COMMENT '预约记录ID，自增主键',
  `manager_id` int NULL DEFAULT NULL COMMENT '管理人ID，关联用户表',
  `user_id` int NOT NULL COMMENT '预约人ID，关联用户表',
  `laboratory_id` int NOT NULL COMMENT '实验室ID，关联实验室表',
  `start_time` datetime NOT NULL COMMENT '开始使用时间',
  `end_time` datetime NOT NULL COMMENT '结束使用时间',
  `purpose` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用途说明',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '待审核' COMMENT '状态：待审核/已批准/使用中/已完成/已取消/已拒绝',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `return_at` datetime NULL DEFAULT NULL COMMENT '实际归还时间',
  PRIMARY KEY (`lab_reservation_id`) USING BTREE,
  UNIQUE INDEX `unique_lab_time_slot`(`laboratory_id` ASC, `start_time` ASC, `end_time` ASC, ((case when (`status` in (_utf8mb4\'æªå®æ\',_utf8mb4\'å¤çä¸­\')) then 1 else NULL end)) ASC) USING BTREE,
  INDEX `idx_lab_status_endtime`(`status` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_lab_lab_status_time`(`laboratory_id` ASC, `status` ASC, `start_time` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_lab_user_status`(`user_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lab_reservation
-- ----------------------------

-- ----------------------------
-- Table structure for laboratory
-- ----------------------------
DROP TABLE IF EXISTS `laboratory`;
CREATE TABLE `laboratory`  (
  `laboratory_id` int NOT NULL AUTO_INCREMENT,
  `image_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地点（如实验楼一层）',
  `laboratory_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '实验室名称',
  `manager_id` int NULL DEFAULT NULL COMMENT '管理员ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`laboratory_id`) USING BTREE,
  INDEX `idx_lab_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of laboratory
-- ----------------------------
INSERT INTO `laboratory` VALUES (1, '实验楼A101.png', '实验楼一层', '实验室A101', 2, '基础化学实验室，配备标准化学实验设备，可用于普通化学实验教学。', '闲置', '2026-04-21 14:16:41');
INSERT INTO `laboratory` VALUES (2, '实验楼B202.png', '实验楼二层', '实验室B202', 2, '物理实验室，配备力学、光学、电学实验设备，可用于大学物理实验。', '闲置', '2026-04-21 14:16:41');
INSERT INTO `laboratory` VALUES (3, '实验楼C303.png', '实验楼三层', '实验室C303', 2, '生物安全实验室，配备生物安全柜、培养箱等设备，需通过安全培训方可使用。', '闲置', '2026-04-21 14:16:41');

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `is_read` tinyint(1) NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`) USING BTREE,
  INDEX `idx_notification_user_read`(`user_id` ASC, `is_read` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------

-- ----------------------------
-- Table structure for opentime
-- ----------------------------
DROP TABLE IF EXISTS `opentime`;
CREATE TABLE `opentime`  (
  `openTime_id` int NOT NULL AUTO_INCREMENT,
  `week` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `laboratory_id` int NULL DEFAULT NULL,
  `device_id` int NULL DEFAULT NULL,
  `manager_id` int NULL DEFAULT NULL,
  `start_time` time NULL DEFAULT NULL,
  `end_time` time NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`openTime_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of opentime
-- ----------------------------

-- ----------------------------
-- Table structure for permission
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`  (
  `permission_id` int NOT NULL AUTO_INCREMENT,
  `permission_level` int NULL DEFAULT NULL,
  `max_reserve_days` int NULL DEFAULT NULL,
  `max_concurrent_devices` int NULL DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`permission_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permission
-- ----------------------------
INSERT INTO `permission` VALUES (1, 1, 7, 3, NULL);
INSERT INTO `permission` VALUES (2, 2, 7, 6, NULL);
INSERT INTO `permission` VALUES (3, 3, 30, 12, NULL);
INSERT INTO `permission` VALUES (4, 4, 30, 30, NULL);
INSERT INTO `permission` VALUES (5, 5, NULL, 30, NULL);

-- ----------------------------
-- Table structure for reservation
-- ----------------------------
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation`  (
  `reservation_id` int NOT NULL AUTO_INCREMENT,
  `manager_id` int NULL DEFAULT NULL,
  `user_id` int NULL DEFAULT NULL,
  `device_id` int NULL DEFAULT NULL,
  `start_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NULL DEFAULT NULL,
  `purpose` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `return_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`reservation_id`) USING BTREE,
  UNIQUE INDEX `unique_device_time_slot`(`device_id` ASC, `start_time` ASC, `end_time` ASC, ((case when (`status` in (_utf8mb4\'æªå®æ\',_utf8mb4\'å¤çä¸­\')) then 1 else NULL end)) ASC) USING BTREE,
  INDEX `idx_res_status_endtime`(`status` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_res_device_status_time`(`device_id` ASC, `status` ASC, `start_time` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_res_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reservation
-- ----------------------------

-- ----------------------------
-- Table structure for scrap
-- ----------------------------
DROP TABLE IF EXISTS `scrap`;
CREATE TABLE `scrap`  (
  `scrap_id` int NOT NULL AUTO_INCREMENT,
  `manager_id` int NULL DEFAULT NULL,
  `device_id` int NULL DEFAULT NULL,
  `approver_id` int NULL DEFAULT NULL,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approver_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `approved_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`scrap_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of scrap
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `account` int NULL DEFAULT NULL COMMENT '用户账号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `identity_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `permission_id` int NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 2022160085, '梁日臻', '$2a$10$Vkko5VRfMOiv8FfaiN/nr.tmfsIq2Z36paCrmeppObluzNchUecma', '学生', 1, '2022160085', 'q1551352154@gmail.com', '正常', NULL);
INSERT INTO `user` VALUES (2, 2022999999, '李四', '$2a$10$b7w4FIQk8mIRoAxK.LAjjuhpdl05HUbXkYqNefN/y/C9fMKw9r4Yy', '教师', 4, '2', NULL, '正常', NULL);

SET FOREIGN_KEY_CHECKS = 1;
