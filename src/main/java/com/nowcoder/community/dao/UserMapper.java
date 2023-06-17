package com.nowcoder.community.dao;

import com.nowcoder.community.pojo.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    String selectFields = "id, username, password, salt, email, type, status, activation_code, header_url, create_time";

    String insertFields = "username, password, salt, email, type, status, activation_code, header_url, create_time";

    @Select("SELECT " + selectFields + " FROM user WHERE id = #{id}")
    User selectById(@Param("id") int id);

    @Select("SELECT " + selectFields + " FROM user WHERE username = #{username}")
    User selectByName(@Param("username") String username);

    @Select("SELECT " + selectFields + " FROM user WHERE email = #{email}")
    User selectByEmail(@Param("email") String email);

    @Insert("INSERT INTO user (" + insertFields + " ) VALUES (#{username}, #{password}, #{salt}, #{email}, #{type}, #{status}, #{activationCode}, #{headerUrl}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(User user);

    @Update("UPDATE user SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") int id, @Param("status") int status);

    @Update("UPDATE user SET header_url = #{headerUrl} WHERE id = #{id}")
    int updateHeader(@Param("id") int id, @Param("headerUrl") String headerUrl);

    @Update("UPDATE user SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") int id, @Param("password") String password);
}
