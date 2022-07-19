package com.wei.estest.doc;

import com.wei.estest.client.ClientTest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;

/**
 * 高级查询操作
 *
 * @author huangw
 * @date 2022/7/19 14:02
 */
public class HighQueryTest {
    public static void main(String[] args) throws IOException {
        RestHighLevelClient client = ClientTest.getClient();

        likeQuery(client);
//        rangeQuery(client);
//        boolQuery(client);
//        excludeQuery(client);
//        sortQuery(client);
//        pageQuery(client);
//        termQuery(client);
//        matchAll(client);
        client.close();

    }


    /**
     * 全查询
     *
     * @param client
     * @throws IOException
     */
    public static void matchAll(RestHighLevelClient client) throws IOException {
        // 创建请求对象
        SearchRequest request = new SearchRequest();
        request.indices("user");

        // 构建查询请求体
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 全查询
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        request.source(searchSourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 查询匹配
        SearchHits hits = response.getHits();
        System.out.println("took:" + response.getTook());
        System.out.println("timeout:" + response.isTimedOut());
        System.out.println("total:" + hits.getTotalHits());
        System.out.println("MaxScore:" + hits.getMaxScore());
        System.out.println("hits========>>");
        for (SearchHit hit : hits) {
            //输出每条查询的结果信息
            System.out.println(hit.getSourceAsString());
        }
        System.out.println("<<========");

    }


    /**
     * 关键字查询
     *
     * @param client
     * @throws IOException
     */
    public static void termQuery(RestHighLevelClient client) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("user");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 关键字查询
        searchSourceBuilder.query(QueryBuilders.termQuery("age", 18));
        request.source(searchSourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 查询匹配
        SearchHits hits = response.getHits();
        System.out.println("took:" + response.getTook());
        System.out.println("timeout:" + response.isTimedOut());
        System.out.println("total:" + hits.getTotalHits());
        System.out.println("MaxScore:" + hits.getMaxScore());
        System.out.println("hits========>>");
        for (SearchHit hit : hits) {
            //输出每条查询的结果信息
            System.out.println(hit.getSourceAsString());
        }
        System.out.println("<<========");
    }


    /**
     * 分页查询
     *
     * @param client
     * @throws IOException
     */
    public static void pageQuery(RestHighLevelClient client) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("user");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // 分页查询  当前页起始索引（第一条数据的顺序号），from
        searchSourceBuilder.from(0);
        // 每页显示多少条size
        searchSourceBuilder.from(2);
        request.source(searchSourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 查询匹配
        SearchHits hits = response.getHits();
        System.out.println("took:" + response.getTook());
        System.out.println("timeout:" + response.isTimedOut());
        System.out.println("total:" + hits.getTotalHits());
        System.out.println("MaxScore:" + hits.getMaxScore());
        System.out.println("hits========>>");
        for (SearchHit hit : hits) {
            //输出每条查询的结果信息
            System.out.println(hit.getSourceAsString());
        }
        System.out.println("<<========");
    }

    /**
     * 排序查询
     *
     * @param client
     * @throws IOException
     */
    public static void sortQuery(RestHighLevelClient client) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("user");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        // 排序操作
        sourceBuilder.sort("age", SortOrder.ASC);

        request.source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 查询匹配
        SearchHits hits = response.getHits();
        System.out.println("took:" + response.getTook());
        System.out.println("timeout:" + response.isTimedOut());
        System.out.println("total:" + hits.getTotalHits());
        System.out.println("MaxScore:" + hits.getMaxScore());
        System.out.println("hits========>>");
        for (SearchHit hit : hits) {
            //输出每条查询的结果信息
            System.out.println(hit.getSourceAsString());
        }
        System.out.println("<<========");
    }

    /**
     * 过滤查询
     *
     * @param client
     * @throws IOException
     */
    public static void excludeQuery(RestHighLevelClient client) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("user");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchAllQuery());

        // 查询字段过滤
        String[] excludes = {};
        String[] includes = {"name", "age"};
        sourceBuilder.fetchSource(includes, excludes);

        request.source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 查询匹配
        SearchHits hits = response.getHits();
        System.out.println("took:" + response.getTook());
        System.out.println("timeout:" + response.isTimedOut());
        System.out.println("total:" + hits.getTotalHits());
        System.out.println("MaxScore:" + hits.getMaxScore());
        System.out.println("hits========>>");
        for (SearchHit hit : hits) {
            //输出每条查询的结果信息
            System.out.println(hit.getSourceAsString());
        }
        System.out.println("<<========");
    }

    /**
     * bool查询
     *
     * @param client
     * @throws IOException
     */
    public static void boolQuery(RestHighLevelClient client) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("user");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 必须包含
        boolQueryBuilder.must(QueryBuilders.matchQuery("age", 18));
        // 一定不包含
        boolQueryBuilder.mustNot(QueryBuilders.matchQuery("name", "lisi"));
        // 可能包含
        boolQueryBuilder.should(QueryBuilders.matchQuery("sex", "女"));

        sourceBuilder.query(boolQueryBuilder);
        request.source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 查询匹配
        SearchHits hits = response.getHits();
        System.out.println("took:" + response.getTook());
        System.out.println("timeout:" + response.isTimedOut());
        System.out.println("total:" + hits.getTotalHits());
        System.out.println("MaxScore:" + hits.getMaxScore());
        System.out.println("hits========>>");
        for (SearchHit hit : hits) {
            //输出每条查询的结果信息
            System.out.println(hit.getSourceAsString());
        }
        System.out.println("<<========");
    }

    /**
     * 范围查询
     *
     * @param client
     * @throws IOException
     */
    public static void rangeQuery(RestHighLevelClient client) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("user");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder("age");
        // 大于等于
        rangeQueryBuilder.gte(15);
        // 小于等于
        rangeQueryBuilder.lte(20);

        sourceBuilder.query(rangeQueryBuilder);
        request.source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 查询匹配
        SearchHits hits = response.getHits();
        System.out.println("took:" + response.getTook());
        System.out.println("timeout:" + response.isTimedOut());
        System.out.println("total:" + hits.getTotalHits());
        System.out.println("MaxScore:" + hits.getMaxScore());
        System.out.println("hits========>>");
        for (SearchHit hit : hits) {
            //输出每条查询的结果信息
            System.out.println(hit.getSourceAsString());
        }
        System.out.println("<<========");
    }

    /**
     * 模糊查询
     *
     * @param client
     * @throws IOException
     */
    public static void likeQuery(RestHighLevelClient client) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("user");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.fuzzyQuery("name", "zhangsan").fuzziness(Fuzziness.ONE));

        request.source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 查询匹配
        SearchHits hits = response.getHits();
        System.out.println("took:" + response.getTook());
        System.out.println("timeout:" + response.isTimedOut());
        System.out.println("total:" + hits.getTotalHits());
        System.out.println("MaxScore:" + hits.getMaxScore());
        System.out.println("hits========>>");
        for (SearchHit hit : hits) {
            //输出每条查询的结果信息
            System.out.println(hit.getSourceAsString());
        }
        System.out.println("<<========");
    }

}
