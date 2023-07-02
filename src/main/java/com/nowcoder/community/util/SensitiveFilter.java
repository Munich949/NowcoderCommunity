package com.nowcoder.community.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SensitiveFilter {

    // 敏感词的替换词
    private static final String REPLACEMENT = "***";

    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive_word.txt"); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));) {
            String keyword;
            while ((keyword = bufferedReader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyWord(keyword);
            }
        } catch (IOException e) {
            log.error("加载敏感词文件失败!", e);
        }
    }

    /**
     * 添加敏感词到前缀树中
     *
     * @param keyword 敏感词
     */
    private void addKeyWord(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            Character c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // 指向子节点 进入下一轮循环
            tempNode = subNode;
            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (!StringUtils.hasLength(text)) {
            return null;
        }
        // 指针1 指向前缀树
        TrieNode tempNode = new TrieNode();
        // 指针2 指向文本 在前
        int begin = 0;
        // 指针3 指向文本 在后
        int position = 0;
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            Character c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)) {
                // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间,指针3都向下走一步
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(c);
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查下一个字符
                position++;
            }
        }

        // 将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    /**
     * 判断字符是否为符号字符
     *
     * @param c 字符
     * @return 是否为符号字符
     */
    private boolean isSymbol(Character c) {
        /*
            !CharUtils.isAsciiAlphanumeric(c)：
            CharUtils.isAsciiAlphanumeric(c)方法用于判断字符是否为ASCII字母或数字。
            所以!CharUtils.isAsciiAlphanumeric(c)表示字符不是ASCII字母或数字。
        */
        /*
            (c < 0x2E80 || c > 0x9FFF)：
            这个条件判断字符的Unicode编码是否在东亚文字范围内。
            0x2E80和0x9FFF分别表示范围的起始和结束，
            所以(c < 0x2E80 || c > 0x9FFF)表示字符的Unicode编码不在东亚文字范围内。
         */
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /**
     * 前缀树节点类
     */
    private class TrieNode {

        // 关键词结束标识
        boolean isKeywordEnd = false;
        // 子节点(key是下级字符,value是下级节点)
        Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}