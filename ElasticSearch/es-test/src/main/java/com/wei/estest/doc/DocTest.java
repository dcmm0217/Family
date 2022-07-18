package com.wei.estest.doc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wei.estest.client.ClientTest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

/**
 * 测试es文档操作
 *
 * @author huangw
 * @date 2022/7/17 14:02
 */
public class DocTest {

    public static void main(String[] args) throws IOException {
        RestHighLevelClient client = ClientTest.getClient();

//        createDoc(client);
//        updateDoc(client);

//        queryDoc(client);
//        deleteDoc(client);

//        insert_batch(client);
        insert_delete(client);
        client.close();
    }


    /**
     * 新建文档操作
     *
     * @param client
     * @throws IOException
     */
    public static void createDoc(RestHighLevelClient client) throws IOException {
        IndexRequest request = new IndexRequest();
        request.index("user").id("1001");

        User user = new User("zhangsan", 18, "男");
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(user);
        // 添加文档数据，数据格式为JSON格式
        request.source(json, XContentType.JSON);
        // 客户端发送请求，获取响应
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        // 测试打印信息
        System.out.println("_index:" + response.getIndex());
        System.out.println("_id:" + response.getId());
        System.out.println("_result:" + response.getResult());

    }

    /**
     * 修改doc操作
     *
     * @param client
     * @throws IOException
     */
    public static void updateDoc(RestHighLevelClient client) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest();
        // 修改配置参数
        updateRequest.index("user").id("1001");
        // 设置请求体
        updateRequest.doc(XContentType.JSON, "sex", "女");
        // 客户端发送请求，获取响应
        UpdateResponse response = client.update(updateRequest, RequestOptions.DEFAULT);

        System.out.println("_index:" + response.getIndex());
        System.out.println("_id:" + response.getId());
        System.out.println("_result:" + response.getResult());
    }

    /**
     * 查询doc操作
     *
     * @param client
     * @throws IOException
     */
    public static void queryDoc(RestHighLevelClient client) throws IOException {
        GetRequest request = new GetRequest().index("user").id("1001");
        // 客户端发送请求，获取相应对象
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        System.out.println("_index:" + response.getIndex());
        System.out.println("_type:" + response.getType());
        System.out.println("_id:" + response.getId());
        System.out.println("source:" + response.getSourceAsString());
    }

    /**
     * 删除doc操作
     *
     * @param client
     * @throws IOException
     */
    public static void deleteDoc(RestHighLevelClient client) throws IOException {
        DeleteRequest request = new DeleteRequest().index("user").id("1001");
        // 客户端发送请求，获取相应对象
        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        // 打印信息
        System.out.println(response.toString());
    }

    /**
     * 批量增加
     *
     * @param client
     * @throws IOException
     */
    public static void insert_batch(RestHighLevelClient client) throws IOException {
        BulkRequest request = new BulkRequest();
        request.add(new IndexRequest("user").id("1001").source(XContentType.JSON, "name", "zhangsan"));
        request.add(new IndexRequest("user").id("1002").source(XContentType.JSON, "name", "lisi"));
        request.add(new IndexRequest("user").id("1003").source(XContentType.JSON, "name", "wangwu"));
        // 客户端发送请求，获取响应对象
        BulkResponse responses = client.bulk(request, RequestOptions.DEFAULT);
        // 打印结果信息
        System.out.println("took:" + responses.getTook());
        System.out.println("items:" + responses.getItems());
    }

    /**
     * 批量增加
     *
     * @param client
     * @throws IOException
     */
    public static void insert_delete(RestHighLevelClient client) throws IOException {
        BulkRequest request = new BulkRequest();
        request.add(new DeleteRequest("user").id("1001"));
        request.add(new DeleteRequest("user").id("1002"));
        request.add(new DeleteRequest("user").id("1003"));
        // 客户端发送请求，获取响应对象
        BulkResponse responses = client.bulk(request, RequestOptions.DEFAULT);
        // 打印结果信息
        System.out.println("took:" + responses.getTook());
        System.out.println("items:" + responses.getItems());
    }


}
