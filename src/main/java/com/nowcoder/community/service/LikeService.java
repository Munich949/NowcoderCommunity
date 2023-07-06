package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LikeService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    // 点赞
    public void like(Integer userId, Integer entityType, Integer entityId, Integer entityUserId) {
        // redis事物 保证赞了一个帖子或评论后 其个人收到的赞也要+1
        stringRedisTemplate.execute(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                // 查看该用户是否点过赞
                Boolean isMember = stringRedisTemplate.opsForSet().isMember(entityLikeKey, String.valueOf(userId));

                operations.multi();

                if (Boolean.TRUE.equals(isMember)) {
                    // 点过 从点赞列表中移除 并将该用户的总点赞数-1
                    stringRedisTemplate.opsForSet().remove(entityLikeKey, String.valueOf(userId));
                    stringRedisTemplate.opsForValue().decrement(userLikeKey);
                } else {
                    // 没点过 加入点赞列表 并将该用户的总点赞数+1
                    stringRedisTemplate.opsForSet().add(entityLikeKey, String.valueOf(userId));
                    stringRedisTemplate.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });
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
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        return stringRedisTemplate.opsForValue().get(userLikeKey) == null ? 0 : Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(userLikeKey)));
    }
}
