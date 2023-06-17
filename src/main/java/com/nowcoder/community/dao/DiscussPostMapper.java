package com.nowcoder.community.dao;

import com.nowcoder.community.pojo.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(@Param("userId") Integer userId);

    int selectDiscussPostRows(@Param("userId") Integer userId);
}
