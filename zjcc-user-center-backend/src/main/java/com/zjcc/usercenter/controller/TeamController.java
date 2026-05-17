package com.zjcc.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjcc.usercenter.common.BaseResponse;
import com.zjcc.usercenter.common.ErrorCode;
import com.zjcc.usercenter.common.ResponseResult;
import com.zjcc.usercenter.exception.BusinessException;
import com.zjcc.usercenter.model.domain.Team;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.model.domain.UserTeam;
import com.zjcc.usercenter.model.dto.TeamQuery;
import com.zjcc.usercenter.model.request.TeamAddRequest;
import com.zjcc.usercenter.model.request.TeamJoinRequest;
import com.zjcc.usercenter.model.request.TeamQuitRequest;
import com.zjcc.usercenter.model.request.TeamUpdateRequest;
import com.zjcc.usercenter.model.vo.TeamUserVO;
import com.zjcc.usercenter.service.TeamService;
import com.zjcc.usercenter.service.UserService;
import com.zjcc.usercenter.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Resource
    private UserTeamService userTeamService;

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
    public BaseResponse<List<TeamUserVO>> listTeams(@RequestBody TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        // 查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        // 查询已加入队伍的人数
        // 1.查询出来的队伍ID
        Set<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toSet());
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UserTeam::getTeamId, teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 根据队伍ID 分组，获得加入该队伍的用户ID集合
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(teamUserVO -> teamUserVO.setHasJoinNum(teamIdUserTeamList.getOrDefault(teamUserVO.getId(), new ArrayList<>()).size()));
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

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestParam Long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验登录状态
        User loginUser = userService.getLoginUser(request);
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return ResponseResult.ok(team);
    }

    /**
     * 查看自己创建的房间
     *
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(HttpServletRequest request) {
        // 登录校验
        userService.getLoginUser(request);
        List<TeamUserVO> teamList = teamService.listMyCreateTeams();
        return ResponseResult.ok(teamList);
    }

    /**
     * 获取我加入的队伍
     *
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(HttpServletRequest request) {
        // 登录校验
        userService.getLoginUser(request);
        List<TeamUserVO> teamList = teamService.listMyJoinTeams();
        return ResponseResult.ok(teamList);
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
    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteTeam(@PathVariable long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResponseResult.ok(true);
    }
}
