package com.nowcoder.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nowcoder.community.pojo.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(Integer entityType, Integer entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已关注");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(Integer entityType, Integer entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注");
    }

    @GetMapping("/followees/{userId}/{page}")
    public String getFollowees(@PathVariable("userId") Integer userId, @PathVariable("page") Integer page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        PageHelper.startPage(page, 5);
        List<Map<String, Object>> list = followService.findFollowees(userId);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list, 5);
        List<Map<String, Object>> followees = pageInfo.getList();
        if (followees != null) {
            for (Map<String, Object> followee : followees) {
                user = (User) followee.get("user");
                followee.put("hasFollowed", hasFollowed(user.getId()));
            }
        }
        model.addAttribute("users", followees);
        model.addAttribute("pageInfo", pageInfo);
        return "site/followee";
    }

    @GetMapping("/followers/{userId}/{page}")
    public String getFollowers(@PathVariable("userId") Integer userId, @PathVariable("page") Integer page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        PageHelper.startPage(page, 5);
        List<Map<String, Object>> list = followService.findFollowers(userId);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list, 5);
        List<Map<String, Object>> followers = pageInfo.getList();
        if (followers != null) {
            for (Map<String, Object> follower : followers) {
                user = (User) follower.get("user");
                follower.put("hasFollowed", hasFollowed(user.getId()));
            }
        }
        model.addAttribute("users", followers);
        model.addAttribute("pageInfo", pageInfo);
        return "site/follower";
    }

    private boolean hasFollowed(Integer userId) {
        User user = hostHolder.getUser();
        if (user == null) {
            return false;
        }
        return followService.hasFollowed(user.getId(), ENTITY_TYPE_USER, userId);
    }
}
