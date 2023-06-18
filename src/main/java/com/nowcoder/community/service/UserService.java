package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.pojo.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    // 域名
    @Value("${community.path.domain}")
    private String domain;

    // 应用上下文
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(Integer id) {
        return userMapper.selectById(id);
    }

    // 注册业务
    public Map<String, Object> register(User user) {
        // 存储注册相关数据出错的map
        Map<String, Object> map = new HashMap<>();

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null!");
        }
        // 有错误就要立即返回 保证业务不继续向下进行
        if (!StringUtils.hasLength(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }
        if (!StringUtils.hasLength(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (!StringUtils.hasLength(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该用户名已存在！");
            return map;
        }

        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已存在！");
            return map;
        }

        // 到这里 用户用于注册的相关数据都没有问题 这时就要将相关数据插入数据库中
        // 设置salt 用随机uuid的前五位
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        // 将用户输入的密码和salt拼接 再进行md5加密 很难破解
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        // 普通用户
        user.setType(0);
        // 未激活状态 后续还需要进行激活
        user.setStatus(0);
        // 生成激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        // 给用户一个随机头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(LocalDateTime.now());
        userMapper.insertUser(user);

        // 向用户填写的邮箱发一个用于激活账号的邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 拼接用于激活的url
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    // 激活用户
    public int activation(Integer userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }
}
