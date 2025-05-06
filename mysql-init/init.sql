USE porthos;

-- 테이블: group_info
CREATE TABLE `group_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '기본 키',
  `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '그룹 이름 (고유값)',
  `created_at` DATETIME NOT NULL COMMENT '생성 일시',
  `updated_at` DATETIME NOT NULL COMMENT '수정 일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='그룹 정보 테이블';

-- 테이블: member_metadata
CREATE TABLE `member_metadata` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '기본 키',
  `slack_url` VARCHAR(50) NOT NULL COMMENT '회원 Slack Webhook URL',
  `slack_noti` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Slack 알림 수신 여부',
  `sms_noti` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'SMS 알림 수신 여부',
  `oncall_noti` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'On-call 알림 수신 여부',
  `created_at` DATETIME NOT NULL COMMENT '생성 일시',
  `updated_at` DATETIME NOT NULL COMMENT '수정 일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원 메타데이터 테이블';

-- 테이블: member
CREATE TABLE `member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '기본 키',
  `member_metadata_id` BIGINT NOT NULL UNIQUE COMMENT '참조 회원 메타데이터 ID',
  `role` VARCHAR(20) NOT NULL COMMENT '회원 역할',
  `email` VARCHAR(50) NOT NULL UNIQUE COMMENT '회원 이메일',
  `password` VARCHAR(255) NOT NULL COMMENT '비밀번호',
  `name` VARCHAR(20) NOT NULL COMMENT '회원 이름',
  `phone` VARCHAR(20) NOT NULL COMMENT '회원 전화번호',
  `created_at` DATETIME NOT NULL COMMENT '생성 일시',
  `updated_at` DATETIME NOT NULL COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`member_metadata_id`) REFERENCES `member_metadata` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원 테이블';

-- 테이블: member_group
CREATE TABLE `member_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '기본 키',
  `member_id` BIGINT NOT NULL COMMENT '회원 ID',
  `group_info_id` BIGINT NOT NULL COMMENT '그룹 ID',
  `created_at` DATETIME NOT NULL COMMENT '생성 일시',
  `updated_at` DATETIME NOT NULL COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_member_group` (`member_id`, `group_info_id`),
  FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`group_info_id`) REFERENCES `group_info` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원-그룹 연결 테이블';

-- 테이블: incident_info
CREATE TABLE `incident_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '기본 키',
  `s3_uuid` VARCHAR(50) NOT NULL COMMENT 'S3 객체 UUID',
  `completion` DATETIME COMMENT '처리 완료 시간',
  `registration_time` DATETIME NOT NULL COMMENT '등록 시각',
  `closing_time` DATETIME COMMENT '장애 종료 시각',
  `created_at` DATETIME NOT NULL COMMENT '생성 일시',
  `updated_at` DATETIME NOT NULL COMMENT '수정 일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='장애 정보 테이블';

-- 테이블: incident_group
CREATE TABLE `incident_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '기본 키',
  `incident_id` BIGINT NOT NULL COMMENT '장애 ID',
  `group_info_id` BIGINT NOT NULL COMMENT '그룹 ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_incident_group` (`incident_id`, `group_info_id`),
  FOREIGN KEY (`incident_id`) REFERENCES `incident_info` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`group_info_id`) REFERENCES `group_info` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='장애-그룹 연결 테이블';

-- 테이블: notification_log
CREATE TABLE `notification_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '기본 키',
  `incident_id` BIGINT NOT NULL COMMENT '장애 ID',
  `member_id` BIGINT NOT NULL COMMENT '회원 ID',
  `created_at` DATETIME NOT NULL COMMENT '생성 일시',
  `updated_at` DATETIME NOT NULL COMMENT '수정 일시',
  `is_verified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '이 장애에 대한 인증 여부',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`incident_id`) REFERENCES `incident_info` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='알림 전파 로그 테이블';
