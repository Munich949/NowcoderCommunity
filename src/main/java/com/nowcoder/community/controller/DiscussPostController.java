package com.nowcoder.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nowcoder.community.pojo.Comment;
import com.nowcoder.community.pojo.DiscussPost;
import com.nowcoder.community.pojo.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(LocalDateTime.now());
        discussPost.setType(0);
        discussPost.setStatus(0);
        discussPost.setCommentCount(0);
        discussPostService.addDiscussPost(discussPost);

        return CommunityUtil.getJSONString(0, "发布成功");
    }

    @GetMapping(path = "/detail/{discussPostId}/{page}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, @PathVariable("page") Integer page) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // 点赞数量
        Long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount", likeCount);
        // 点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeStatus", likeStatus);

        PageHelper.startPage(page, 5);

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 帖子的评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId());

        PageInfo<Comment> pageInfo = new PageInfo<>(commentList, 5);

        List<Comment> comments = pageInfo.getList();

        // 评论VO(View Object)列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                // 一条评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 一条评论
                commentVo.put("comment", comment);
                // 此条评论的作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                // 点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 此条评论的回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 此条评论的单条回复
                        replyVo.put("reply", reply);
                        // 此条评论的单条回复的作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);
                        // 回复的目标有可能不是此条评论的作者 而是回复 回复了此条评论的其他人
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentsCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("comments", commentVoList);

        return "site/discuss-detail";
    }

}
