package com.nowcoder.community.controller;

import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.pojo.Event;
import com.nowcoder.community.pojo.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/like")
    @ResponseBody
    public String like(Integer entityType, Integer entityId, Integer entityUserId, Integer postId) {
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);

        // 数量
        Long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        if (entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数
            String postKey = RedisKeyUtil.getPostKey();
            stringRedisTemplate.opsForSet().add(postKey, String.valueOf(postId));
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}
