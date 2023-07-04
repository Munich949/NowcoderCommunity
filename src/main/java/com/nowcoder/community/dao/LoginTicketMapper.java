package com.nowcoder.community.dao;

import com.nowcoder.community.pojo.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {
    String selectFields = "id, user_id, ticket, status, expired";

    @Select("SELECT " + selectFields + " FROM login_ticket WHERE ticket = #{ticket}")
    LoginTicket selectByTicket(String ticket);

    @Insert("INSERT INTO login_ticket(user_id, ticket, status, expired) VALUES (#{userId}, #{ticket}, #{status}, #{expired})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Update("UPDATE login_ticket SET status = #{status} WHERE ticket = #{ticket}")
    int updateStatus(@Param("ticket") String ticket, @Param("status") int status);
}
