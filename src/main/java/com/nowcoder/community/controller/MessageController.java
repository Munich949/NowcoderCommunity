package com.nowcoder.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nowcoder.community.pojo.Message;
import com.nowcoder.community.pojo.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @GetMapping("/letter/list/{page}")
    public String getLetterList(@PathVariable("page") Integer page, Model model) {
        User user = hostHolder.getUser();
        PageHelper.startPage(page, 10);
        // 该用户的所有私信
        List<Message> list = messageService.findConversations(user.getId());
        PageInfo<Message> pageInfo = new PageInfo<>(list, 5);
        List<Message> conversationList = pageInfo.getList();
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                // 单条私信
                map.put("conversation", message);
                // 私信对话数量
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                // 未读消息数
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                // 目标对象
                int targetId = Objects.equals(user.getId(), message.getFromId()) ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }

        }
        model.addAttribute("conversations", conversations);
        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        model.addAttribute("pageInfo", pageInfo);

        return "site/letter";
    }

    @GetMapping("/letter/detail/{conversationId}/{page}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, @PathVariable("page") Integer page, Model model) {
        PageHelper.startPage(page, 10);
        // 私信列表
        List<Message> list = messageService.findLetters(conversationId);
        PageInfo<Message> pageInfo = new PageInfo<>(list, 5);
        List<Message> letterList = pageInfo.getList();
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                // 私信内容
                map.put("letter", message);
                // 发送人
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));
        model.addAttribute("pageInfo", pageInfo);

        List<Integer> ids = getLetterIds(letterList);
        System.out.println(ids);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "site/letter-detail";
    }

    /**
     * 根据conversationId(111_112)拆解发送人和接收人
     *
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    // 获取未读的消息id
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if ((Objects.equals(hostHolder.getUser().getId(), message.getToId())) && (message.getStatus() == 0)) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    @PostMapping("/letter/send")
    @ResponseBody
    public String addMessage(String toName, String content) {
        User target = userService.findUserByUsername(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在!");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setStatus(0);
        message.setContent(content);
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setCreateTime(LocalDateTime.now());
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }
}
