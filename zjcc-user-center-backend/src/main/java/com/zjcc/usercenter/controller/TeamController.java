package com.zjcc.usercenter.controller;

import com.zjcc.usercenter.common.BaseResponse;
import com.zjcc.usercenter.common.ErrorCode;
import com.zjcc.usercenter.common.ResponseResult;
import com.zjcc.usercenter.exception.BusinessException;
import com.zjcc.usercenter.model.domain.Team;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.model.dto.TeamQuery;
import com.zjcc.usercenter.model.request.TeamAddRequest;
import com.zjcc.usercenter.model.request.TeamJoinRequest;
import com.zjcc.usercenter.model.request.TeamQuitRequest;
import com.zjcc.usercenter.model.request.TeamUpdateRequest;
import com.zjcc.usercenter.model.vo.TeamUserVO;
import com.zjcc.usercenter.service.TeamService;
import com.zjcc.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author zjchenchang
 * @createDate 2026/5/3 13:29
 * @description 队伍
 */
@RestController
@RequestMapping("/api/team")
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    // 创建队伍
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResponseResult.ok(teamId);
    }

    // 查询队伍
    @PostMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        return ResponseResult.ok(teamList);
    }

    // 修改队伍信息
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamRequest, HttpServletRequest request) {
        if (teamRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResponseResult.ok(true);
    }

    // 加入队伍
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResponseResult.ok(result);
    }

    // 退出队伍
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResponseResult.ok(result);
    }

    // 解散队伍
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResponseResult.ok(true);
    }
}
