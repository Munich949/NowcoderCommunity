package com.nowcoder.community.dao;

import com.nowcoder.community.pojo.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {
    String selectFields = "id, user_id, entity_type, entity_id, target_id, content, status, create_time";

    String insertFields = "user_id, entity_type, entity_id, target_id, content, status, create_time";

    @Select("SELECT " + selectFields + " FROM comment WHERE entity_type = #{entityType} and entity_id = #{entityId} ORDER BY create_time")
    List<Comment> selectCommentsByEntity(@Param("entityType") Integer entityType, @Param("entityId") Integer entityId);

    @Select("SELECT COUNT(id) FROM comment WHERE entity_type = #{entityType} and entity_id = #{entityId}")
    int selectCommentsCountByEntity(@Param("entityType") Integer entityType, @Param("entityId") Integer entityId);

    @Insert("INSERT INTO comment (" + insertFields + ") VALUES (#{userId}, #{entityType}, #{entityId}, #{targetId}, #{content}, #{status}, #{createTime})")
    int insertComment(Comment comment);

    @Select("SELECT " + selectFields + " FROM comment WHERE id = #{id}")
    Comment selectCommentsCountById(@Param("id") Integer id);
}
