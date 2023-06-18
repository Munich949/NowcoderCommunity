package com.nowcoder.community.controller;

import com.nowcoder.community.pojo.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    UserService userService;

    @GetMapping(value = {"/register"})
    public String getRegisterPage() {
        return "site/register";
    }

    @GetMapping(value = {"/login"})
    public String getLoginPage() {
        return "site/login";
    }

    @PostMapping(value = {"/register"})
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);

        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活！");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            log.info("model={}",model.getAttribute("user"));
            return "site/register";
        }
    }

    @GetMapping(value = "/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") Integer userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "site/operate-result";
    }
}
