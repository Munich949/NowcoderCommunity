package com.nowcoder.community.dao;

import com.nowcoder.community.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {
    String selectFields = "id, user_id, entity_type, entity_id, target_id, content, status, create_time";

    String insertFields = "user_id, entity_type, entity_id, target_id, content, status, create_time";

    @Select("SELECT " + selectFields + " FROM comment WHERE entity_type = #{entityType} and entity_id = #{entityId} ORDER BY create_time")
    List<Comment> selectCommentsByEntity(Integer entityType, Integer entityId);

    @Select("SELECT COUNT(id) FROM comment WHERE entity_type = #{entityType} and entity_id = #{entityId}")
    int selectCommentsCountByEntity(Integer entityType, Integer entityId);
}
