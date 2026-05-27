package com.wizv.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wizv.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
