package com.zjcc.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用 分页请求参数包装类
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -4162304142710323660L;

    /**
     * 页面大小
     */
    protected Integer pageSize;

    /**
     * 当前第几页
     */
    protected Integer pageNum;
}