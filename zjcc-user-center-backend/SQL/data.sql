-- auto-generated definition
create table user (
    id           bigint auto_increment comment '主键ID'
        primary key,
    username     varchar(50)  default '' null comment '昵称',
    userAccount  varchar(50)   not null comment '登录账号',
    avatarUrl    varchar(255) default '' null comment '头像地址',
    gender       tinyint      default 0 null comment '性别（0-未知，1-男，2-女）',
    profile      varchar(512) null comment '个人简介',
    userPassword varchar(100)  not null comment '密码（建议加密存储）',
    phone        varchar(20)  default '' null comment '电话号码',
    email        varchar(50)  default '' null comment '邮箱地址',
    userStatus   int          default 0 null comment '用户状态（0-正常，其他值可自定义异常状态）',
    createTime   datetime     default CURRENT_TIMESTAMP null comment '创建时间（自动插入当前时间）',
    updateTime   datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间（自动更新当前时间）',
    isDelete     tinyint      default 0 null comment '是否删除（0-未删除，1-已删除）- 逻辑删除',
    userRole     tinyint      default 0 null comment '用户角色（0-普通用户，1-管理员）',
    planetCode   varchar(512) null comment '用户编号',
    tags         varchar(1024) not null comment '标签列表'
) comment '用户表';


-- auto-generated definition
create table team (
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)       not null comment '队伍名称',
    description varchar(1024) null comment '描述',
    maxNum      int      default 1 not null comment '最大人数',
    expireTime  datetime null comment '过期时间',
    userId      bigint null comment '用户id(（队长 id）)',
    status      int      default 0 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512) null comment '密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0 not null comment '是否删除'
) comment '队伍';

-- auto-generated definition
create table user_team (
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint null comment '用户id',
    teamId     bigint null comment '队伍id',
    joinTime   datetime null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0 not null comment '是否删除'
) comment '用户队伍关系';

