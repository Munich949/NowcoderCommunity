package com.nowcoder.community.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
    private Integer id;
    private String username;
    private String password;
    // 随机生成的字符串，它会被添加到用户输入的密码中，然后再进行加密
    private String salt;
    private String email;
    // 0-普通用户; 1-超级管理员; 2-版主;
    private Integer type;
    // 0-未激活; 1-已激活;
    private Integer status;
    private String activationCode;
    private String headerUrl;
    private LocalDateTime createTime;
}
