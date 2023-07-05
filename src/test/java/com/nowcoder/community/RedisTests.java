package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testStrings() {
        String redisKey = "test:count";

        stringRedisTemplate.opsForValue().set(redisKey, "1");

        System.out.println(stringRedisTemplate.opsForValue().get(redisKey));
        System.out.println(stringRedisTemplate.opsForValue().increment(redisKey));
        System.out.println(stringRedisTemplate.opsForValue().decrement(redisKey));
    }
}