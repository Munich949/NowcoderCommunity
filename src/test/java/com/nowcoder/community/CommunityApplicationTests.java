package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;

@SpringBootTest
class CommunityApplicationTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;


    @Autowired
    private SensitiveFilter sensitiveFilter;

//    @Test
//    void test1() {
//        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149);
//        for (DiscussPost discussPost : discussPosts) {
//            System.out.println(discussPost);
//        }
//
//
//        int rows = discussPostMapper.selectDiscussPostRows(0);
//        System.out.println(rows);
//    }
//
//    @Test
//    public void test2() {
//        mailClient.sendMail("2493929776@qq.com", "杨波你好", "杨波大帅哥");
//    }
//
//    @Test
//    public void test3() {
//        Context context = new Context();
//        context.setVariable("username", "Munich");
//
//        String content = templateEngine.process("/mail/demo", context);
//
//        System.out.println(content);
//
//        mailClient.sendMail("2493929776@qq.com", "HTML", content);
//    }
//
//    @Test
//    public void testInsertLoginTicket() {
//        LoginTicket loginTicket = new LoginTicket();
//        loginTicket.setUserId(101);
//        loginTicket.setTicket("abc");
//        loginTicket.setStatus(0);
//        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
//
//
//        loginTicketMapper.insertLoginTicket(loginTicket);
//    }

//    @Test
//    public void testSelectLoginTicket() {
//        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
//        System.out.println(loginTicket);
//
//        loginTicketMapper.updateStatus("abc", 1);
//        loginTicket = loginTicketMapper.selectByTicket("abc");
//        System.out.println(loginTicket);
//    }

//    @Test
//    public void testSelectLetters() {
//        List<Message> list = messageMapper.selectConversations(111, 0, 20);
//        for (Message message : list) {
//            System.out.println(message);
//        }
//
//        int count = messageMapper.selectConversationCount(111);
//        System.out.println(count);
//
//        list = messageMapper.selectLetters("111_112", 0, 10);
//        for (Message message : list) {
//            System.out.println(message);
//        }
//
//        count = messageMapper.selectLetterCount("111_112");
//        System.out.println(count);
//
//        count = messageMapper.selectLetterUnreadCount(131, "111_131");
//        System.out.println(count);
//
//    }

    @Test
    public void testSensitiveFilter() {
        String text = "这里可以赌博,可以嫖娼,可以吸毒,可以操你妈,哈哈哈，傻逼!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "这里可以☆赌☆博☆,可以☆嫖☆娼☆,可以☆操☆你☆妈☆,哈哈哈，☆傻☆逼☆!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }


}
