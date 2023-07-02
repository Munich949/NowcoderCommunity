package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.pojo.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    public List<Comment> findCommentsByEntity(Integer entityType, Integer entityId) {
        return commentMapper.selectCommentsByEntity(entityType, entityId);
    }

    public int findCommentsCount(Integer entityType, Integer entityId) {
        return commentMapper.selectCommentsCountByEntity(entityType, entityId);
    }
}
