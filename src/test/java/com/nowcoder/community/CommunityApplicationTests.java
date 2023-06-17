package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.pojo.DiscussPost;
import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@SpringBootTest
class CommunityApplicationTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    void test1() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }


        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Test
    public void test2() {
        mailClient.sendMail("2493929776@qq.com", "杨波你好", "杨波大帅哥");
    }

    @Test
    public void test3() {
        Context context = new Context();
        context.setVariable("username", "Munich");

        String content = templateEngine.process("/mail/demo", context);

        System.out.println(content);

        mailClient.sendMail("2493929776@qq.com", "HTML", content);
    }

}
