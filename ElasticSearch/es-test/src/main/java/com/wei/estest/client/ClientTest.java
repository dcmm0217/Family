package com.wei.estest.client;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

/**
 * @author huangw
 * @date 2022/7/17 14:02
 */
public class ClientTest {

    public static void main(String[] args) throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost",9200,"http"))
        );

        client.close();
    }

    public static RestHighLevelClient getClient(){
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost",9200,"http"))
        );
    }
}
