-- auto-generated definition
create table picture
(
    id           bigint auto_increment comment 'id  唯一标识'
        primary key,
    url          varchar(1024)                       null comment '图片地址',
    userId       bigint                              null comment '关联的用户id',
    name         varchar(256)                        null comment '图片名称',
    introduction  varchar(512)                        null comment '图片描述',
    category     int                                 null comment '图片分类',
    tags         varchar(256)                        null comment '图片标签',
    picSize      bigint                              null comment '图片大小',
    picWidth     int       default 0                 null comment '图片宽度',
    picHeight    int       default 0                 null comment '图片高度',
    picScale     int       default 0                 null comment '图片宽高比例',
    pirFormat    int       default 0                 null comment '图片格式',
    editTime     datetime  default CURRENT_TIMESTAMP null comment '编辑时间',
    createTime   datetime  default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint   default 0                 null comment '是否删除',
    isPublic     tinyint   default 0                 null comment '是否公开',
    INDEX idx_name(name),  -- 关于图片名称的高效索引
    INDEX idx_introduction(introduction),  -- 关于图片描述的高效索引
    INDEX idx_tags(tags),  -- 关于图片标签的高效索引
    INDEX idx_category(category),  -- 关于图片分类的高效索引
    INDEX idx_userId(userId)  -- 关于用户id的高效索引
)
    comment '图片' collate = utf8mb4_unicode_ci;

alter table picture
    -- 添加新的列
    add column reviewStatus int default 0 not null comment '审核状态 0未审核 1审核通过 2审核不通过',
    add column reviewTime datetime default null comment '审核时间',
    add column reviewUserId bigint default null comment '审核人id',
    add column reviewMessage varchar(256) default null comment '审核备注';

-- 创建基于reviewStatus的索引
create index idx_reviewStatus on picture(reviewStatus);

alter table picture
    add column spaceId bigint null comment "空间id(为空表示公共空间)";

create index idx_spaceId on picture(spaceId);