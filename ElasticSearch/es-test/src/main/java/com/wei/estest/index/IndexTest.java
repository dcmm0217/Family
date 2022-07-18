package com.wei.estest.index;

import com.wei.estest.client.ClientTest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;

import java.io.IOException;

/**
 * @author huangw
 * @date 2022/7/17 14:02
 */
public class IndexTest {
    public static void main(String[] args) throws IOException {
        RestHighLevelClient client = ClientTest.getClient();
        createIndex(client);
    }

    /**
     * 创建索引
     * @param client
     * @throws IOException
     */
    public static void createIndex(RestHighLevelClient client) throws IOException {

        CreateIndexRequest createIndexRequest = new CreateIndexRequest("user");

        CreateIndexResponse response = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

        System.out.println("操作状态" + response.isAcknowledged());
    }
}
