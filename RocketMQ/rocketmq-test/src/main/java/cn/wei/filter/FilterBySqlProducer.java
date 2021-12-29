package cn.wei.filter;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

public class FilterBySqlProducer {
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("wei-producer");
        producer.setNamesrvAddr("localhost:9876");
        producer.start();

        for (int i = 0; i < 3; i++) {
            byte[] bytes = ("Hi" + i).getBytes();
            Message message = new Message("myTopic", "myTag", bytes);
            // 设置消息属性，便于过滤
            message.putUserProperty("age", i + "");
            SendResult sendResult = producer.send(message);
            System.out.println(sendResult);
        }
        producer.shutdown();
    }
}
