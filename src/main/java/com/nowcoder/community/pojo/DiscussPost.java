package com.nowcoder.community.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DiscussPost {
    private Integer id;
    private Integer UserId;
    private String title;
    private String content;
    private Integer type;
    private Integer status;
    private LocalDateTime createTime;
    private Integer commentCount;
    private double score;
}
