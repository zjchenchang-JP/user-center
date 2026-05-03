package com.zjcc.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjcc.usercenter.common.ErrorCode;
import com.zjcc.usercenter.exception.BusinessException;
import com.zjcc.usercenter.mapper.TeamMapper;
import com.zjcc.usercenter.model.TeamStatusEnum;
import com.zjcc.usercenter.model.domain.Team;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.model.domain.UserTeam;
import com.zjcc.usercenter.model.dto.TeamQuery;
import com.zjcc.usercenter.model.vo.TeamUserVO;
import com.zjcc.usercenter.service.TeamService;
import com.zjcc.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author 86187
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2026-05-03 13:22:59
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private TeamMapper teamMapper;

    @Resource
    UserTeamService userTeamService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 3. 校验信息
        //   a. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum <= 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }
        //   b. 队伍标题 <= 20
        String teamName = team.getName();
        if (StringUtils.isBlank(teamName) || teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //   c. 如果有描述，则描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //   d. status 是否公开（int）不传默认为 0（公开）
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不符合要求");
        }
        //   e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            String password = team.getPassword();
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //   f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间早于当前时间");
        }
        //   g. 校验用户最多创建 5 个队伍
        final long userId = loginUser.getId();
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(teamQueryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建5个队伍");
        }
        // 4-5插入是关联操作 必须用事务控制
        // 4. 插入队伍信息到队伍表
        team.setId(null);// 主键自增
        team.setUserId(userId);
        boolean isSave = this.save(team);
        Long teamId = team.getId();
        if (!isSave) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        // 5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        isSave = userTeamService.save(userTeam);
        if (!isSave) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        // 查询队伍列表
        // 分页展示队伍列表，根据名称、最大人数等搜索队伍 P0，信息流中不展示已过期的队伍
        // 从请求参数中取出队伍名称等查询条件，如果存在则作为查询条件
        // 拼接查询条件
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();

        // 前端传入了查询条件
        if (teamQuery != null) {
            Long teamId = teamQuery.getId();
            if (teamId != null && teamId > 0) {
                queryWrapper.eq(Team::getId, teamId);
            }
            // 可以通过某个关键词同时对名称和描述查询
            // 传入了查询关键词 才拼接该条件
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like(Team::getName, searchText)
                        .or().like(Team::getDescription, searchText));
            }
            // 根据队伍名查询
            if (StringUtils.isNotBlank(teamQuery.getName())) {
                queryWrapper.like(Team::getName, teamQuery.getName());
            }
            // 根据描述查询
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like(Team::getDescription, description);
            }
            // 查询最大人数相等
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq(Team::getMaxNum, maxNum);
            }
            // 根据创建人来查询
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq(Team::getUserId, userId);
            }
            // 根据队伍状态查询
            // 只有管理员才能查看加密还有非公开的房间
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                // 如果前端没传入该值，默认只查询公开房间
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq(Team::getStatus, statusEnum.getValue());
        }

        // 不展示已过期的队伍（根据过期时间筛选）
        //expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt(Team::getExpireTime, new Date())
                .or().isNull(Team::getExpireTime));

        // 5. 关联查询已加入队伍的用户信息

        // 6. 关联查询已加入队伍的用户信息（可能会很耗费性能，建议大家用自己写 SQL 的方式实现）

        return Collections.emptyList();
    }
}




