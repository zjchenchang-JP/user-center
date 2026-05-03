package com.zjcc.usercenter.service;

import com.zjcc.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.model.dto.TeamQuery;
import com.zjcc.usercenter.model.vo.TeamUserVO;

import java.util.List;

/**
* @author 86187
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2026-05-03 13:22:59
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team 新建队伍参数
     * @param loginUser 当前登录用户
     * @return 创建成功 队伍ID
     */
    long addTeam(Team team, User loginUser);

    /**
     * 查询队伍
     * @param teamQuery 查询条件
     * @param isAdmin 是否是管理员
     * @return 符号条件的队伍信息
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);
}
