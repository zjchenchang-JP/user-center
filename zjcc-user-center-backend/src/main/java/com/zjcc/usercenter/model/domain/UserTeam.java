package com.zjcc.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户队伍关系
 * @TableName user_team
 */
@TableName(value ="user_team")
@Data
public class UserTeam implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 加入时间
     */
    private Date joinTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 生成 SQL 语句时忽略这个字段，不会将其作为数据库表的列来处理
     * 常见使用场景
     * 不存在的数据库列 - 实体类中有但数据库表中没有的字段
     * 关联查询结果 - 存储来自其他表或复杂查询的结果
     * 临时计算字段 - 运行时计算的属性，不需要持久化
     * 前端展示字段 - 仅用于返回给前端展示
     */
    @TableField(exist = false) // 标记非数据库字段
    private static final long serialVersionUID = 1L;
}