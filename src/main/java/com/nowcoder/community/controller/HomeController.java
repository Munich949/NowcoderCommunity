package com.nowcoder.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nowcoder.community.pojo.DiscussPost;
import com.nowcoder.community.pojo.User;
import com.nowcoder.community.service.DiscussService;
import com.nowcoder.community.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class HomeController {

    @Autowired
    private UserService userService;
    @Autowired
    private DiscussService discussService;

    @GetMapping(value = {"/index", "/index/{page}"})
    public String getIndexPage(Model model, @PathVariable(required = false) Integer page) {
        if (page == null) {
            page = 1;
        }
        PageHelper.startPage(page, 10);
        List<DiscussPost> list = discussService.findDiscussPosts(0);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        for (DiscussPost post : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("post", post);
            User user = userService.findUserById(post.getUserId());
            map.put("user", user);
            discussPosts.add(map);
        }

        PageInfo<DiscussPost> pageInfo = new PageInfo<>(list, 5);
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("pageInfo", pageInfo);
        return "index";
    }
}
