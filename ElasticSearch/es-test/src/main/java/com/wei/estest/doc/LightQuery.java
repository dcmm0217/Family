package com.wei.estest.doc;

import com.wei.estest.client.ClientTest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.io.IOException;
import java.util.Map;

/**
 * 高亮查询和聚合查询
 *
 * @author huangw
 * @date 2022/7/17 14:02
 */
public class LightQuery {
    public static void main(String[] args) throws IOException {
        RestHighLevelClient client = ClientTest.getClient();


        groupQuery(client);
//        mixQuery(client);
//        lightQuery(client);

        client.close();
    }


    /**
     * 高亮查询
     *
     * @param client
     * @throws IOException
     */
    public static void lightQuery(RestHighLevelClient client) throws IOException {
        SearchRequest request = new SearchRequest().indices("user");
        // 创建查询请求体构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建查询方式 高亮查询
        TermsQueryBuilder termQueryBuilder = QueryBuilders.termsQuery("name", "zhangsan");
        // 设置查询方式
        searchSourceBuilder.query(termQueryBuilder);
        // 构建高亮字段
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font color='red'>");//设置标签前缀
        highlightBuilder.postTags("</font>");//设置标签后缀
        highlightBuilder.field("name");//设置高亮字段
        // 设置高亮构建对象
        searchSourceBuilder.highlighter(highlightBuilder);
        // 设置请求体
        request.source(searchSourceBuilder);
        // 客户端发送请求，获取响应对象
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.打印响应结果
        SearchHits hits = response.getHits();
        System.out.println("took::" + response.getTook());
        System.out.println("time_out::" + response.isTimedOut());
        System.out.println("total::" + hits.getTotalHits());
        System.out.println("max_score::" + hits.getMaxScore());
        System.out.println("hits::::>>");
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            System.out.println(sourceAsString);
            //打印高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            System.out.println(highlightFields);
        }
        System.out.println("<<::::");
    }


    /**
     * 聚合查询
     *
     * @param client
     * @throws IOException
     */
    public static void mixQuery(RestHighLevelClient client) throws IOException {
        // 高亮查询
        SearchRequest request = new SearchRequest().indices("user");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.aggregation(AggregationBuilders.max("maxAge").field("age"));
        request.source(sourceBuilder);
        SearchResponse response = client.search(request,RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        System.out.println(response);
    }

    /**
     * 分组查询
     * @param client
     * @throws IOException
     */
    public static void groupQuery(RestHighLevelClient client) throws IOException{
        SearchRequest request = new SearchRequest().indices("user");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 通过age分组查询
        sourceBuilder.aggregation(AggregationBuilders.terms("age_groupby").field("age"));
        request.source(sourceBuilder);
        SearchResponse response = client.search(request,RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        System.out.println(response);
    }
}
