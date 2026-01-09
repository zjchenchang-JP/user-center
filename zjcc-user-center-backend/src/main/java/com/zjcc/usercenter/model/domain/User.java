package com.zjcc.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.Data;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="`user`")
@Data
public class User {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 昵称
     */
    private String username;

    /**
     * 登录账号
     */
    private String userAccount;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 性别（0-未知，1-男，2-女）
     */
    private Integer gender;

    /**
     * 密码（建议加密存储）
     */
    private String userPassword;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 用户状态（0-正常，其他值可自定义异常状态）
     */
    private Integer userStatus;

    /**
     * 创建时间（自动插入当前时间）
     */
    private Date createTime;

    /**
     * 更新时间（自动更新当前时间）
     */
    private Date updateTime;

    /**
     * 是否删除（0-未删除，1-已删除）- 逻辑删除
     * 逻辑删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 用户角色（0-普通用户，1-管理员）
     */
    private Integer userRole;

    /**
     * 用户编号
     */
    private String planetCode;
}