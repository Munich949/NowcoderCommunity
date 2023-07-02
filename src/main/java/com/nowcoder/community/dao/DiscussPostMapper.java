package com.nowcoder.community.dao;

import com.nowcoder.community.pojo.DiscussPost;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    String selectFields = "id, user_id, title, content, type, status, create_time, comment_count, score";

    String insertFields = "user_id, title, content, type, status, create_time, comment_count, score";

    List<DiscussPost> selectDiscussPosts(@Param("userId") Integer userId);

    int selectDiscussPostRows(@Param("userId") Integer userId);

    @Insert("INSERT INTO discuss_post(" + insertFields + ") VALUES (#{userId},#{title},#{content},#{type},#{status},#{createTime},#{commentCount},#{score})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDiscussPost(DiscussPost discussPost);

    @Select("SELECT " + selectFields + " FROM discuss_post WHERE id = #{id}")
    DiscussPost selectDiscussPostById(@Param("id") Integer id);
}
