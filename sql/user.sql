-- auto-generated definition
create table user
(
    id           bigint auto_increment comment 'id  唯一标识'
        primary key,
    userName     varchar(256)                        null comment '用户名称',
    userAccount  varchar(256)                        null comment '账号',
    avatar    varchar(1024)                       null comment '用户头像',
    gender       tinyint                             null comment '性别',
    profile      varchar(512)                        null comment '用户个人简介',
    phone        varchar(128)                        null comment '电话号码',
    email        varchar(512)                        null comment '邮箱',
    userStatus   int       default 0                 null comment '状态',
    userPassword varchar(512)                        null comment '登录密码',
    editTime     datetime  default CURRENT_TIMESTAMP null comment '编辑时间',
    createTime   datetime  default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint   default 0                 null comment '是否删除',
    userRole     int       default 0                 null comment '用户身份 0 普通用户 1 管理员用户',
    vipExpireTime datetime null comment 'vip过期时间',
    vipCode varchar(256) null comment 'vip兑换码',
    vipNumber bigint null comment 'vip编号',
    shareCode varchar(256) default null comment '分享码',
    inviteUserId bigint default null comment '邀请人id',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_username (username)
)
    comment '用户' collate = utf8mb4_unicode_ci;