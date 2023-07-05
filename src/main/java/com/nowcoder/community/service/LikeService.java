package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    // 点赞
    public void like(Integer userId, Integer entityType, Integer entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 查看该用户是否点过赞
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(entityLikeKey, String.valueOf(userId));
        if (Boolean.TRUE.equals(isMember)) {
            // 点过 从点赞列表中移除
            stringRedisTemplate.opsForSet().remove(entityLikeKey, String.valueOf(userId));
        } else {
            // 没点过 加入点赞列表
            stringRedisTemplate.opsForSet().add(entityLikeKey, String.valueOf(userId));
        }
    }
    // 查询实体点赞数量
    public Long findEntityLikeCount(Integer entityType, Integer entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return stringRedisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(Integer userId, Integer entityType, Integer entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 返回int 而不返回 boolean 的原因是 以后如果开发 “踩” 的功能 可以返回 -1
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(entityLikeKey, String.valueOf(userId))) ? 1 : 0;
    }

    // 查询某个用户获得的赞
//    public int findUserLikeCount(int userId) {
//        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
//        Integer count = (Integer) stringRedisTemplate.opsForValue().get(userLikeKey);
//        return count == null ? 0 : count.intValue();
//    }
}
