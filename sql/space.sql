create table if not exists space (
    id bigint auto_increment comment "id" primary key,
    spaceName varchar(256) comment "空间名称",
    spaceLevel int default 0 null comment "空间等级：0-普通空间 1-专业版 2-企业版",
    spaceType int default 0 null comment "空间类型：0-个人空间 1-团队空间",
    maxSize bigint default 0 null comment "最大容量",
    totalSize bigint default 0 null comment "已使用容量",
    maxCount bigint default 0 null comment "最大存储数量",
    totalCount bigint default 0 null comment "已使用数量",
    userId bigint default 0 null comment "创建用户id",
    createTime datetime default CURRENT_TIMESTAMP not null comment "创建时间",
    editTime datetime default CURRENT_TIMESTAMP not null comment "编辑时间",
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment "更新时间",
    isDelete tinyint default 0 not null comment "是否删除",
    index idx_spaceName (spaceName),     -- 通过空间名称查找空间
    index idx_spaceLevel (spaceLevel),   -- 通过空间等级查找空间
    index idx_userId (userId)           -- 通过用户id查找空间
) comment "空间表" collate = utf8mb4_unicode_ci;