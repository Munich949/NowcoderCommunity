package com.nowcoder.community.service;

import com.alibaba.fastjson2.JSON;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.pojo.LoginTicket;
import com.nowcoder.community.pojo.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 域名
    @Value("${community.path.domain}")
    private String domain;

    // 应用上下文
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(Integer id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
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

    // 登录
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (!StringUtils.hasLength(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (!StringUtils.hasLength(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        String ticketValue = JSON.toJSONString(loginTicket);
        stringRedisTemplate.opsForValue().set(ticketKey, ticketValue);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        String ticketValue = stringRedisTemplate.opsForValue().get(ticketKey);
        LoginTicket loginTicket = JSON.parseObject(ticketValue, LoginTicket.class);
        loginTicket.setStatus(1);
        ticketValue = JSON.toJSONString(loginTicket);
        stringRedisTemplate.opsForValue().set(ticketKey, ticketValue);
    }

    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        String ticketValue = stringRedisTemplate.opsForValue().get(ticketKey);

        return JSON.parseObject(ticketValue, LoginTicket.class);
    }

    public int updateHeader(Integer userId, String headerUrl) {
//        return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    // 重置密码
    public Map<String, Object> resetPassword(String email, String password) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (org.apache.commons.lang3.StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证邮箱
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱尚未注册!");
            return map;
        }

        // 重置密码
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);

        map.put("user", user);
        return map;
    }

    // 修改密码
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (!StringUtils.hasLength(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空!");
            return map;
        }
        if (!StringUtils.hasLength(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }

        // 验证原始密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原密码输入有误!");
            return map;
        }

        // 更新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);
        clearCache(userId);

        return map;
    }

    public User findUserByUsername(String username) {
        return userMapper.selectByName(username);
    }

    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        String userValue = stringRedisTemplate.opsForValue().get(userKey);
        return JSON.parseObject(userValue, User.class);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        String userValue = JSON.toJSONString(user);
        stringRedisTemplate.opsForValue().set(userKey, userValue, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        stringRedisTemplate.delete(redisKey);
    }


    public Collection<? extends GrantedAuthority> getAuthorities(Integer userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add((GrantedAuthority) () -> switch (user.getType()) {
            case 1 -> AUTHORITY_ADMIN;
            case 2 -> AUTHORITY_MODERATOR;
            default -> AUTHORITY_USER;
        });
        return authorities;
    }
}
