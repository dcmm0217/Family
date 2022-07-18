package com.wei.estest.index;

import com.wei.estest.client.ClientTest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;

import java.io.IOException;

/**
 * 测试es索引操作
 *
 * @author huangw
 * @date 2022/7/17 14:02
 */
public class IndexTest {
    public static void main(String[] args) throws IOException {
        RestHighLevelClient client = ClientTest.getClient();
//        createIndex(client);

//        getIndex(client);

        deleteIndex(client);

        client.close();
    }

    /**
     * 创建索引
     *
     * @param client
     * @throws IOException
     */
    public static void createIndex(RestHighLevelClient client) throws IOException {

        CreateIndexRequest createIndexRequest = new CreateIndexRequest("user");

        CreateIndexResponse response = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

        System.out.println("操作状态" + response.isAcknowledged());
    }

    /**
     * 查询索引
     *
     * @param client
     * @throws IOException
     */
    public static void getIndex(RestHighLevelClient client) throws IOException {

        GetIndexRequest getIndexRequest = new GetIndexRequest("user");
        GetIndexResponse response = client.indices().get(getIndexRequest, RequestOptions.DEFAULT);
        System.out.println("aliases:" + response.getAliases());
        System.out.println("mappings:" + response.getMappings());
        System.out.println("settings:" + response.getSettings());
    }


    /**
     * 删除索引
     *
     * @param client
     * @throws IOException
     */
    public static void deleteIndex(RestHighLevelClient client) throws IOException {
        DeleteIndexRequest deleteRequest = new DeleteIndexRequest("user");
        AcknowledgedResponse response = client.indices().delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println("删除标志：" + response.isAcknowledged());
    }
}
