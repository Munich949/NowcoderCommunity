package com.nowcoder.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nowcoder.community.pojo.DiscussPost;
import com.nowcoder.community.pojo.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private UserService userService;
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    // 处理首页请求的方法，支持分页
    @GetMapping(value = {"/index", "/index/{page}"})
    public String getIndexPage(Model model, @PathVariable(required = false) Integer page,
                               @RequestParam(name = "orderMode", defaultValue = "0") Integer orderMode) {
        // 如果page参数为null，则默认为第一页
        if (page == null) {
            page = 1;
        }
        // 使用PageHelper进行分页设置，每页显示10条数据
        PageHelper.startPage(page, 10);
        // 调用discussService的findDiscussPosts方法获取帖子列表
        List<DiscussPost> discussPostList = discussPostService.findDiscussPosts(0, orderMode);
        PageInfo<DiscussPost> pageInfo = new PageInfo<>(discussPostList, 5);
        List<DiscussPost> list = pageInfo.getList();
        // 创建一个List<Map<String, Object>>用于存储帖子和对应的用户信息
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        // 遍历帖子列表，将每个帖子和对应的用户信息存储在一个Map中，然后添加到discussPosts列表中
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);

                Long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }

        // 使用PageInfo对象对帖子列表进行分页处理，每页显示5个分页导航
        // 将帖子列表和分页信息添加到Model对象中
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("orderMode", orderMode);
        // 返回视图名称"index"
        return "index";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "error/500";
    }

    @GetMapping("/denied")
    public String getDeniedPage() {
        return "/error/404";
    }
}