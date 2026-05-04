package com.zjcc.usercenter.mapper;

import com.zjcc.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjcc.usercenter.model.vo.TeamUserVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 86187
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2026-05-03 13:22:59
* @Entity com.zjcc.usercenter.model.domain.Team
*/
public interface TeamMapper extends BaseMapper<Team> {

    /**
     * 查询队伍列表（包含创建人信息、已加入人数、当前用户是否已加入）
     *
     * @param teamIds 队伍ID列表
     * @param loginUserId 当前登录用户ID
     * @return 队伍用户VO列表
     */
    List<TeamUserVO> listTeamUserVOByIds(@Param("teamIds") List<Long> teamIds, @Param("loginUserId") Long loginUserId);
}




