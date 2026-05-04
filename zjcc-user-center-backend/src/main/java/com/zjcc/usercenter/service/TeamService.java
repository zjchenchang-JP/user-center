package com.zjcc.usercenter.service;

import com.zjcc.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.model.dto.TeamQuery;
import com.zjcc.usercenter.model.request.TeamJoinRequest;
import com.zjcc.usercenter.model.request.TeamQuitRequest;
import com.zjcc.usercenter.model.request.TeamUpdateRequest;
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
     * @return 符合条件的队伍列表
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest 更新条件
     * @param loginUser 当前登录用户
     * @return 是否更新成功
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest 加入队伍请求参数
     * @param loginUser 当前登录用户
     * @return 是否加入成功
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest 请求参数 队伍ID
     * @param loginUser 当前登录用户
     * @return 是否退出成功
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散队伍
     * @param id 队伍ID
     * @param loginUser 当前登录用户
     * @return 是否删除成功
     */
    boolean deleteTeam(long id, User loginUser);
}
