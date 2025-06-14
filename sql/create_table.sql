create database if not exists fig_space;
use fig_space;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

create table if not exists figure
(
    id          bigint auto_increment   primary key comment 'id',
    url         varchar(512)            not null comment '图片 url 地址',
    name        varchar(128)            not null comment '图片名称',
    intro       varchar(512)            null comment '介绍',
    category    varchar(64)             null comment '分类',
    tags        varchar(512)            null comment '标签：JSON数组',
    figSize     bigint                  null comment '图片大小',
    figWidth    int                     null comment '图片宽度',
    figHeight    int                     null comment '图片高度',
    figScale    double                  null comment '图片宽高比',
    figFormat   varchar(32)             null comment '图片格式',
    userId      bigint                  not null comment '用户 id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime    datetime default CURRENT_TIMESTAMP not null comment '编辑时间（用户通过系统的编辑时间）',
    updateTime  datetime default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间（包括了直接改数据库）',
    isDelete    tinyint  default 0  not null comment '逻辑删除',
    INDEX idx_name (name),
    INDEX idx_intro (intro),
    INDEX idx_category (category),
    INDEX idx_tags (tags),
    INDEX idx_userId (userId)
) comment '图片' collate = utf8mb4_unicode_ci;

ALTER TABLE figure
    -- 添加新列
    ADD COLUMN reviewStatus INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    ADD COLUMN reviewMessage VARCHAR(512) NULL COMMENT '审核信息',
    ADD COLUMN reviewerId BIGINT NULL COMMENT '审核人 ID',
    ADD COLUMN reviewTime DATETIME NULL COMMENT '审核时间';

-- 创建基于 reviewStatus 列的索引
CREATE INDEX idx_reviewStatus ON figure (reviewStatus);

