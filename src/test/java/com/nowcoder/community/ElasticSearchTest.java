package com.nowcoder.community;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.pojo.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.*;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    @Qualifier("client")
    private RestHighLevelClient restHighLevelClient;

    //判断某id的文档（数据库中的行）是否存在
    @Test
    public void testExist() {
        boolean exists = discussPostRepository.existsById(1101);
        System.out.println(exists);
    }

    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0));
    }

    //修改一下
    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我在干嘛？赶紧灌水");
        discussPostRepository.save(post);
    }


    // 删除数据
    @Test
    public void testDelete() {
//        discussPostRepository.deleteById(231);//删除一条数据
        discussPostRepository.deleteAll();//删除所有数据
    }

    //不带高亮的查询
    @Test
    public void noHighlightQuery() throws IOException {
        // 指定要查询的索引名称
        SearchRequest searchRequest = new SearchRequest("discusspost");

        //构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                // 构建一个多字段查询，查询指定的关键字“互联网寒冬”是否出现在title和content字段中
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery是精准查询：searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                // 结果的排序方式
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 指定从哪条开始查询
                .from(0)
                // 需要查出的总记录条数
                .size(10);

        // 将SearchSourceBuilder对象设置为SearchRequest对象的查询条件
        searchRequest.source(searchSourceBuilder);

        // 使用RestHighLevelClient类的search()方法来执行查询，并获取查询结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            list.add(discussPost);
        }
        System.out.println(list.size());
        for (DiscussPost post : list) {
            System.out.println(post);
        }
    }

    @Test
    public void highlightQuery() throws Exception {
        // 指定要查询的索引名称
        SearchRequest searchRequest = new SearchRequest("discusspost");
        Map<String, Object> res = new HashMap<>();

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");

        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                // 构建一个多字段查询，查询指定的关键字是否出现在title和content字段中
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // 结果的排序方式
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 指定从哪条开始查询
                .from(0)
                // 需要查出的总记录条数
                .size(10)
                // 高亮
                .highlighter(highlightBuilder);

        // 将SearchSourceBuilder对象设置为SearchRequest对象的查询条件
        searchRequest.source(searchSourceBuilder);

        // 使用RestHighLevelClient类的search()方法来执行查询，并获取查询结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<DiscussPost> list = new ArrayList<>();
        long total = searchResponse.getHits().getTotalHits().value;
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            // 处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                discussPost.setContent(contentField.getFragments()[0].toString());
            }
            list.add(discussPost);
        }
        res.put("list", list);
        res.put("total", total);
        if (res.get("list") != null) {
            for (DiscussPost post : list = (List<DiscussPost>) res.get("list")) {
                System.out.println(post);
            }
            System.out.println(res.get("total"));
        }
    }
}
