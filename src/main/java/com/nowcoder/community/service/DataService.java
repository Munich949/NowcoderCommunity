package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DataService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    // 将指定的IP计入UV
    public void recordUV(String ip) {
        String uvKey = RedisKeyUtil.getUVKey(sdf.format(new Date()));
        stringRedisTemplate.opsForHyperLogLog().add(uvKey, ip);
    }

    // 统计指定日期范围内的UV
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("时间参数不能为空!");
        }

        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String uvKey = RedisKeyUtil.getUVKey(sdf.format(calendar.getTime()));
            keyList.add(uvKey);
            calendar.add(Calendar.DATE, 1);
        }

        // 合并数据
        String uvKey = RedisKeyUtil.getUVKey(sdf.format(start), sdf.format(end));
        stringRedisTemplate.opsForHyperLogLog().union(uvKey, keyList.toArray(new String[0]));

        // 返回统计结果
        return stringRedisTemplate.opsForHyperLogLog().size(uvKey);
    }

    // 将指定用户计入DAU
    public void recordDAU(Integer userId) {
        String dauKey = RedisKeyUtil.getDAUKey(sdf.format(new Date()));
        stringRedisTemplate.opsForValue().setBit(dauKey, userId, true);
    }

    // 统计指定日期范围内的DAU
    public Long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("时间参数不能为空!");
        }

        // 整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String dauKey = RedisKeyUtil.getDAUKey(sdf.format(calendar.getTime()));
            keyList.add(dauKey.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        // 进行OR运算
        return stringRedisTemplate.execute((RedisCallback<Long>) connection -> {
            String dauKey = RedisKeyUtil.getDAUKey(sdf.format(start), sdf.format(end));
            // 二位byte数组的含义是：ketList是一个<byte[]>类型的List，将List转换成byte[]就是一个一维数组，再有一个泛型就是二维数组
            connection.bitOp(RedisStringCommands.BitOperation.OR, dauKey.getBytes(), keyList.toArray(new byte[0][0]));
            // 计算OR运算后的结果中设置为1的位的数量，即计算DAU的数量
            return connection.bitCount(dauKey.getBytes());
        });
    }
}
