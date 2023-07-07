package com.nowcoder.community.service;

import com.nowcoder.community.pojo.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserService userService;

    // 关注
    public void follow(Integer userId, Integer entityType, Integer entityId) {
        stringRedisTemplate.execute(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

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
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

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
    public Long findFollowerCount(Integer entityType, Integer entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return stringRedisTemplate.opsForZSet().zCard(followerKey);
    }

    // 查询当前用户是否已经关注了该实体
    public boolean hasFollowed(Integer userId, Integer entityType, Integer entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return stringRedisTemplate.opsForZSet().score(followeeKey, String.valueOf(entityId)) != null;
    }

    // 查询某用户关注的人
    public List<Map<String, Object>> findFollowees(Integer userId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<String> targetIds = stringRedisTemplate.opsForZSet().reverseRange(followeeKey, 0, -1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (String id : targetIds) {
            Integer targetId = Integer.valueOf(id);
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = stringRedisTemplate.opsForZSet().score(followeeKey, String.valueOf(targetId));
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    // 查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(Integer userId) {
        String followeeKey = RedisKeyUtil.getFollowerKey(userId, ENTITY_TYPE_USER);
        Set<String> targetIds = stringRedisTemplate.opsForZSet().reverseRange(followeeKey, 0, -1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (String id : targetIds) {
            Integer targetId = Integer.valueOf(id);
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = stringRedisTemplate.opsForZSet().score(followeeKey, String.valueOf(targetId));
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}
