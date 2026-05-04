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
import com.zjcc.usercenter.model.request.TeamJoinRequest;
import com.zjcc.usercenter.model.request.TeamQuitRequest;
import com.zjcc.usercenter.model.request.TeamUpdateRequest;
import com.zjcc.usercenter.model.vo.TeamUserVO;
import com.zjcc.usercenter.model.vo.UserVO;
import com.zjcc.usercenter.service.TeamService;
import com.zjcc.usercenter.service.UserService;
import com.zjcc.usercenter.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;


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
        // // 设置分页默认值
        // if (teamQuery == null) {
        //     teamQuery = new TeamQuery();
        // }
        // if (teamQuery.getPageNum() == null || teamQuery.getPageNum() <= 0) {
        //     teamQuery.setPageNum(1);
        // }
        // if (teamQuery.getPageSize() == null || teamQuery.getPageSize() <= 0) {
        //     teamQuery.setPageSize(10);
        // }

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
        }

        // 根据队伍状态查询（移到 if 外部，确保必定执行）
        // 只有管理员才能查看加密还有非公开的房间
        // TODO 即使非公开房间，创建人自己也能看到
        Integer status = (teamQuery != null) ? teamQuery.getStatus() : null;
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            // 如果前端没传入该值，默认只查询公开房间
            statusEnum = TeamStatusEnum.PUBLIC;
        }
        if (!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        queryWrapper.eq(Team::getStatus, statusEnum.getValue());

        // 不展示已过期的队伍（根据过期时间筛选）
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt(Team::getExpireTime, new Date())
                .or().isNull(Team::getExpireTime));
        // 查询展示符合条件的队伍
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        // 关联查询创建人的用户信息
        ArrayList<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏封装VO
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        // TODO 关联查询已加入队伍的用户信息（可能会很耗费性能，可以用自己写 SQL 的方式实现）
        // // 提取所有队伍ID
        // List<Long> teamIds = teamList.stream()
        //        .map(Team::getId)
        //        .collect(java.util.stream.Collectors.toList());
        // // ✅ 使用自定义 SQL 关联查询：创建人信息、已加入人数、当前用户是否已加入
        // // 注意：这里传入 null 作为 loginUserId，因为 listTeams 方法签名中没有该参数
        // // 如果需要判断"当前用户是否已加入"，需要修改方法签名传入 loginUserId
        // List<TeamUserVO> teamUserVOList = teamMapper.listTeamUserVOByIds(teamIds, null);

        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamRequest, User loginUser) {
        // 1. 判断请求参数是否为空
        if (teamRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 查询队伍是否存在
        Long id = teamRequest.getId();
        if (id == null || id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 3. 只有管理员或者队伍的创建者可以修改
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 4. 如果用户传入的新值和老值一致，就不用 update 了
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamRequest, updateTeam);

        // 比对关键字段，如果没有变化则不更新
        if (isTeamUnchanged(oldTeam, updateTeam)) {
            return true;
        }

        // 5. 如果队伍状态改为加密，必须要有密码
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamRequest.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(teamRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须设置密码");
            }
        }
        // 6. 更新成功
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 队伍必须存在，只能加入未满、未过期的队伍
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null && teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // TODO this调用，会影响事务吗
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 3. 禁止加入私有的队伍
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        // 5. 如果加入的队伍是加密的，必须密码匹配才可以
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        // 1. 用户最多加入 5 个队伍
        long loginUserId = loginUser.getId();

        // ========== 分布式锁：防止并发问题 ==========
        // 加锁范围：从检查用户加入队伍数量开始，到插入记录结束
        // 锁的粒度：按 用户ID + 队伍ID 加锁，避免用户同时加入不同队伍时互斥
        String lockKey = "team:join:" + loginUserId + ":" + teamId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(0, 30, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请勿重复操作");
            }

            // 检查用户加入队伍数量
            LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTeam::getUserId, loginUserId);
            long hasJoinNum = userTeamService.count(queryWrapper);
            if (hasJoinNum >= 5) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多加入5个队伍");
            }

            // 不能加入自己的队伍，不能重复加入已加入的队伍（幂等性）
            // 问题场景：
            // - 用户快速多次点击"加入"按钮
            // - 分布式环境下，多个请求同时通过校验
            // - 结果：插入多条相同的 user_team 记录
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTeam::getUserId, loginUserId);
            queryWrapper.eq(UserTeam::getTeamId, teamId);
            long hasUserJoinTeam = userTeamService.count(queryWrapper);
            if (hasUserJoinTeam > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
            }

            // 已加入队伍的人数
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTeam::getTeamId, teamId);
            long teamHasJoinNum = userTeamService.count(queryWrapper);
            if (teamHasJoinNum >= team.getMaxNum()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
            }
            // 6. 新增队伍 - 用户关联信息
            UserTeam userTeam = new UserTeam();
            userTeam.setUserId(loginUserId);
            userTeam.setTeamId(teamId);
            userTeam.setJoinTime(new Date());
            return userTeamService.save(userTeam);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("加入队伍发生异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "操作失败");
        } finally {
            // 释放锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        // 1. 校验请求参数
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 校验队伍是否存在
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 3. 校验我是否已加入队伍
        int userId = loginUser.getId();
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId, teamId);
        queryWrapper.eq(UserTeam::getUserId, userId);
        long hasJoinTeam = userTeamService.count(queryWrapper);
        if (hasJoinTeam == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能退出未加入的队伍");
        }
        // 4. 如果队伍
        //   a. 只剩一人，队伍解散
        long hasJoinTeamNums = this.countTeamUserByTeamId(teamId);
        if (hasJoinTeamNums == 1) {
            // 删除队伍
            this.removeById(teamId);
        } else {
            // b. 还有其他人
            //   ⅰ. 如果是队长退出队伍，队长权限转移给第二早加入的用户 —— 先来后到只用取 id 最小的 2 条数据
            if (userId == team.getUserId()) {
                LambdaQueryWrapper<UserTeam> userTeamQueryWrapper = new LambdaQueryWrapper<>();
                userTeamQueryWrapper.eq(UserTeam::getTeamId, teamId);
                userTeamQueryWrapper.last("order by id limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() < 2) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                Team newTeam = new Team();
                newTeam.setUserId(userTeamList.get(1).getUserId());
                newTeam.setId(teamId);
                boolean result = this.updateById(newTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
            //   ⅱ. 非队长，自己退出队伍 合并到外层逻辑 移除User-Team关系
        }
        // 退出队伍的本质：移除 User-Team关系表的 关系
        return userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        // 查询队伍是否存在
        Team team = getTeamById(id);
        Long teamId = team.getId();
        // 队长才能解散队伍
        // !team.getUserId().equals(loginUser.getId()) // 类型不同 返回false
        if (Objects.equals(team.getUserId(),loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH,"没有权限解散");
        }
        // 移除 所有加入 该队伍 的关联信息
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId, teamId);
        boolean result = userTeamService.remove(queryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }


    /**
     * 判断队伍信息是否发生变化
     * 比对关键字段，如果都相同则认为没有变化
     *
     * @param oldTeam 旧队伍信息
     * @param newTeam 新队伍信息
     * @return true 表示没有变化，false 表示有变化
     */
    private boolean isTeamUnchanged(Team oldTeam, Team newTeam) {
        return Objects.equals(oldTeam.getName(), newTeam.getName()) &&
                Objects.equals(oldTeam.getDescription(), newTeam.getDescription()) &&
                Objects.equals(oldTeam.getExpireTime(), newTeam.getExpireTime()) &&
                Objects.equals(oldTeam.getStatus(), newTeam.getStatus()) &&
                Objects.equals(oldTeam.getPassword(), newTeam.getPassword());
    }


    /**
     * 获取某队伍当前人数
     *
     * @param teamId 队伍ID
     * @return 队伍人数
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    /**
     * 根据 id 获取队伍信息
     *
     * @param teamId 队伍ID
     * @return 获取到的队伍
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

}




