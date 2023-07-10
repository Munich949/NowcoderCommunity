package com.nowcoder.community.dao;

import com.nowcoder.community.pojo.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper {
    String selectFields = "id, from_id, to_id, conversation_id, content, status, create_time";

    String insertFields = "from_id, to_id, conversation_id, content, status, create_time";


    // 查询当前用户的会话列表,针对每个会话只返回一条最新的私信.
    @Select("SELECT " + selectFields + " FROM message WHERE id IN (SELECT MAX(id) FROM message WHERE status != 2 AND from_id != 1 AND (from_id = #{userId} OR to_id = #{userId}) GROUP BY conversation_id) ORDER BY id DESC")
    List<Message> selectConversations(@Param("userId") Integer userId);

    // 查询当前用户的会话数量.
    @Select("SELECT COUNT(m.maxid) FROM (SELECT MAX(id) AS maxid FROM message WHERE status != 2 AND from_id != 1 AND (from_id = #{userId} OR to_id = #{userId}) GROUP BY conversation_id ) AS m")
    int selectConversationCount(@Param("userId") Integer userId);

    // 查询某个会话所包含的私信列表.
    @Select("SELECT " + selectFields + " FROM message WHERE status != 2 AND from_id != 1 AND conversation_id = #{conversationId}")
    List<Message> selectLetters(@Param("conversationId") String conversationId);

    // 查询某个会话所包含的私信数量.
    @Select("SELECT COUNT(id) FROM message WHERE status != 2 AND from_id != 1 AND conversation_id = #{conversationId}")
    int selectLetterCount(@Param("conversationId") String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(@Param("userId") Integer userId, @Param("conversationId") String conversationId);

    // 新增消息
    @Insert("INSERT INTO message (" + insertFields + ") VALUES (#{fromId}, #{toId}, #{conversationId}, #{content}, #{status}, #{createTime})")
    int insertMessage(Message message);

    // 修改消息的状态（未读->已读）
    int updateStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status);

    // 查询某个主题下最新的通知
    @Select("SELECT " + selectFields + " FROM message WHERE id IN (SELECT MAX(id) FROM message WHERE status != 2 AND from_id = 1 AND to_id = #{userId} AND conversation_id = #{topic})")
    Message selectLatestNotice(Integer userId, String topic);

    // 查询某个主题所包含的通知数量
    @Select("SELECT COUNT(id) FROM message WHERE status != 2 AND from_id = 1 AND to_id = #{userId} AND conversation_id = #{topic}")
    int selectNoticeCount(Integer userId, String topic);

    // 查询未读的通知的数量
    int selectNoticeUnreadCount(Integer userId, String topic);

    // 查询某个主题所包含的通知列表
    @Select("SELECT " + selectFields + " FROM message WHERE status != 2 AND from_id = 1 AND to_id = #{userId} AND conversation_id = #{topic} ORDER BY create_time DESC")
    List<Message> selectNotices(Integer userId, String topic);

}
