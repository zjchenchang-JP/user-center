package com.zjcc.usercenter.controller;

import com.zjcc.usercenter.common.BaseResponse;
import com.zjcc.usercenter.common.ErrorCode;
import com.zjcc.usercenter.common.ResponseResult;
import com.zjcc.usercenter.exception.BusinessException;
import com.zjcc.usercenter.model.domain.Team;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.model.dto.TeamQuery;
import com.zjcc.usercenter.model.request.TeamAddRequest;
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

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.loginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResponseResult.ok(teamId);
    }

    @PostMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        return ResponseResult.ok(teamList);
    }

}
