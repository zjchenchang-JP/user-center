package com.zjcc.usercenter.model.dto;

import com.zjcc.usercenter.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据传输封装类
 *
 */
@Data
@EqualsAndHashCode(callSuper = true) // 调用父类的 equals/hashCode，将父类字段也纳入比较和哈希计算
public class TeamQuery extends PageRequest {

    private static final long serialVersionUID = -5503771642388562339L;
    /**
     * 队伍id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 搜索关键字（同时从队伍名称和描述搜索）
     */
    private String searchText;
}