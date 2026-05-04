package com.zjcc.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入队伍请求体
 *
 * @author zjchenchang
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -24663018187059425L;

    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}