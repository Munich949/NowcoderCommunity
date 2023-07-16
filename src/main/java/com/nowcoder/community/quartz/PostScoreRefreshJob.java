package com.nowcoder.community.quartz;

import com.nowcoder.community.pojo.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticSearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

@Component
@Slf4j
public class PostScoreRefreshJob implements Job, CommunityConstant {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    // 牛客纪元
    private static final Date EPOCH;

    static {
        try {
            EPOCH = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String postKey = RedisKeyUtil.getPostKey();
        BoundSetOperations<String, String> operations = stringRedisTemplate.boundSetOps(postKey);

        if (operations.size() == 0) {
            log.info("[任务取消]，没有需要刷新的帖子");
            return;
        }
        log.info("[任务开始]，正在刷新帖子的分数：" + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer.valueOf(Objects.requireNonNull(operations.pop()))));
        }
        log.info("[任务结束]，帖子分数刷新完毕!");
    }

    private void refresh(Integer postId) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(postId);

        if (discussPost == null) {
            log.error("该帖子不存在：id = {}", postId);
            return;
        }

        // 是否精华
        boolean wonderful = discussPost.getStatus() == 1;
        // 评论数量
        Integer commentCount = discussPost.getCommentCount();
        // 点赞数量
        Long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double weight = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;

        LocalDateTime localDateTime = discussPost.getCreateTime();
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        Date date = Date.from(instant);

        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(weight, 1)
                + (date.getTime() - EPOCH.getTime()) / (1000 * 3600 * 24));

        // 更新帖子分数
        discussPostService.updateScore(postId, score);

        // 同步搜索数据
        discussPost.setScore(score);
        elasticSearchService.saveDiscussPost(discussPost);
    }
}
