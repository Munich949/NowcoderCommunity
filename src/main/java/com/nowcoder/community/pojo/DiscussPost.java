package com.nowcoder.community.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(indexName = "discusspost")
public class DiscussPost {
    @Id
    private Integer id;
    @Field(type = FieldType.Integer)
    private Integer UserId;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    @Field(type = FieldType.Integer)
    private Integer type;
    @Field(type = FieldType.Integer)
    private Integer status;
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;
    @Field(type = FieldType.Integer)
    private Integer commentCount;
    @Field(type = FieldType.Double)
    private double score;
}
