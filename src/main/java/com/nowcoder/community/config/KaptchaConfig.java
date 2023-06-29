package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {
    @Bean
    public Producer kaptchaProducer() {
        // 创建一个Properties对象，用于存储配置属性
        Properties properties = new Properties();

        // 设置验证码图片的宽度和高度
        properties.setProperty("kaptcha.image.width", "100");
        properties.setProperty("kaptcha.image.height", "40");

        // 设置验证码文本字体的大小和颜色
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");

        // 设置验证码文本字符的范围和长度
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYAZ");
        properties.setProperty("kaptcha.textproducer.char.length", "4");

        // 设置验证码干扰元素的实现类
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");

        // 创建一个DefaultKaptcha对象
        DefaultKaptcha kaptcha = new DefaultKaptcha();

        // 使用Config对象将Properties对象中的配置应用到DefaultKaptcha对象中
        Config config = new Config(properties);
        kaptcha.setConfig(config);

        // 返回配置好的验证码生成器
        return kaptcha;
    }
}