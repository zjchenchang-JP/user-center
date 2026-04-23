package com.zjcc.usercenter.service.impl;

import com.zjcc.usercenter.model.domain.Tag;
import com.zjcc.usercenter.mapper.TagMapper;
import com.zjcc.usercenter.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
* @author 86187
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2026-02-26 21:43:45
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

}

