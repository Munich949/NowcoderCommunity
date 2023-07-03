package com.nowcoder.community.dao;

import com.nowcoder.community.pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper {
    String selectFields = "id, from_id, to_id, conversation_id, content, status, create_time";

    // 查询当前用户的会话列表,针对每个会话只返回一条最新的私信.
    @Select("SELECT " + selectFields + " FROM message WHERE id IN (SELECT MAX(id) FROM message WHERE status != 2 AND from_id != 1 AND (from_id = #{userId} OR to_id = #{userId}) GROUP BY conversation_id) ORDER BY id DESC")
    List<Message> selectConversations(Integer userId);

    // 查询当前用户的会话数量.
    @Select("SELECT COUNT(m.maxid) FROM (SELECT MAX(id) AS maxid FROM message WHERE status != 2 AND from_id != 1 AND (from_id = #{userId} OR to_id = #{userId}) GROUP BY conversation_id ) AS m")
    int selectConversationCount(Integer userId);

    // 查询某个会话所包含的私信列表.
    @Select("SELECT " + selectFields + " FROM message WHERE status != 2 AND from_id != 1 AND conversation_id = #{conversationId}")
    List<Message> selectLetters(String conversationId);

    // 查询某个会话所包含的私信数量.
    @Select("SELECT COUNT(id) FROM message WHERE status != 2 AND from_id != 1 AND conversation_id = #{conversationId}")
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(Integer userId, String conversationId);
}