package cn.wei.batch;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量消息生产者
 */
public class BatchProducer {
    public static void main(String[] args) throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer("wei-batch-producer");
        producer.setNamesrvAddr("localhost:9876");
        // 指定要发送的消息的最大大小，默认是4M 仅修改这个是没有的，还要修改broker加载配置文件中的 maxMessageSize 配置属性
        producer.setMaxMessageSize(8 * 1024 * 1024);

        producer.start();

        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            byte[] bytes = (("hi" + i)).getBytes();
            Message message = new Message("TopicBatch", "*", bytes);
            messages.add(message);
        }
        MessageListSplitter messageListSplitter = new MessageListSplitter(messages);
        while (messageListSplitter.hasNext()) {
            try {
                List<Message> messageList = messageListSplitter.next();
                producer.send(messageList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
