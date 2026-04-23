package com.zjcc.usercenter.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author zjchenchang
 * @createDate 2026/4/21 20:15
 */
@Data
public class XingQiuTableUserInfo {
    /**
     * 星球编号
     */
    @ExcelProperty("成员编号")
    private String planetCode;

    /**
     * 用户昵称
     */
    @ExcelProperty("成员昵称")
    private String userName;
}
