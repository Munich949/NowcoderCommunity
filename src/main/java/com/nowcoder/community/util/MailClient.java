package com.nowcoder.community.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Component
public class MailClient {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            // 发送方
            helper.setFrom(from);
            // 接收方
            helper.setTo(to);
            // 主题
            helper.setSubject(subject);
            // 正文
            helper.setText(content);
            javaMailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            log.error("邮件发送失败！" + e.getMessage());
        }
    }
}
