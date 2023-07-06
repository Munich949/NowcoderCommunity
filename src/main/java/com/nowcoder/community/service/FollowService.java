package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class FollowService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 关注
    public void follow(Integer userId, Integer entityType, Integer entityId) {
        stringRedisTemplate.execute(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFolloweeKey(entityType, entityId);

                stringRedisTemplate.multi();

                stringRedisTemplate.opsForZSet().add(followeeKey, String.valueOf(entityId), System.currentTimeMillis());
                stringRedisTemplate.opsForZSet().add(followerKey, String.valueOf(userId), System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    // 取消关注
    public void unfollow(Integer userId, Integer entityType, Integer entityId) {
        stringRedisTemplate.execute(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFolloweeKey(entityType, entityId);

                stringRedisTemplate.multi();

                stringRedisTemplate.opsForZSet().remove(followeeKey, String.valueOf(entityId));
                stringRedisTemplate.opsForZSet().remove(followerKey, String.valueOf(userId));
                return operations.exec();
            }
        });
    }

    // 查询关注的实体数量
    public Long findFolloweeCount(Integer userId, Integer entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return stringRedisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询实体的粉丝数量
    public Long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return stringRedisTemplate.opsForZSet().zCard(followerKey);
    }

    // 查询当前用户是否已经关注了该实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return stringRedisTemplate.opsForZSet().score(followeeKey, String.valueOf(entityId)) != null;
    }

}
