package cn.wei.generate;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

public class SyncProducer {
    public static void main(String[] args) throws Exception {
        // 创建一个producer，参数为producer group的名称
        DefaultMQProducer producer = new DefaultMQProducer("wei-producer");
        // 指定nameserver的地址
        producer.setNamesrvAddr("localhost:9876");
        // 设置失败重发次数为3 ，默认是2次
        producer.setRetryTimesWhenSendFailed(3);
        // 设置发送超时时限为5s，默认为3s
        producer.setSendMsgTimeout(5000);

        // 开启生产者
        producer.start();

        // 生产并发送100条消息
        for (int i = 0; i < 100; i++) {
            byte[] bytes = ("Hi" + i).getBytes();
            Message message = new Message("topicA", "*", bytes);
            // 为消息指定key
            message.setKeys("key-" + i);
            // 发送消息
            SendResult sendResult = producer.send(message);
            System.out.println(sendResult);
        }
        producer.shutdown();
    }
}
