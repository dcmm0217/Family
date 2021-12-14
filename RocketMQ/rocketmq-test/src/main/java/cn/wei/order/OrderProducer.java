package cn.wei.order;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.List;

/**
 * @author huang
 * @date 2021/12/14
 **/
public class OrderProducer {
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("wei-producer");
        producer.setNamesrvAddr("localhost:9876");
        // 若为全局有序消息,则需要指定默认消费队列,分区有序则不用指定。
        producer.setDefaultTopicQueueNums(1);

        producer.start();

        for (int i = 0; i < 100; i++) {
            Integer orderId = i;
            byte[] bytes = ("hi " + i).getBytes();
            Message message = new Message("topicA","*",bytes);
            // 将orderId 作为消息key
            message.setKeys(orderId.toString());
            // 同步发送
            SendResult sendResult = producer.send(message, new MessageQueueSelector() {
                // 具体的选择算法在该方法中定义
                @Override
                public MessageQueue select(List<MessageQueue> list, Message message, Object args) {
                    // 以下是使用消息key作为选择key的算法
                    String keys = message.getKeys();
                    Integer id = Integer.valueOf(keys);

                    // 以下是使用args作为选择key的算法
                    // 选择key 这里就是外部传进来的args 也就是orderId = i
                    // Integer id = (Integer) args;

                    // 筛选数据
                    int index = id % list.size();
                    // 返回要发送的队列
                    return list.get(index);
                }
            },orderId);
            System.out.println(sendResult);
        }
        producer.shutdown();
    }
}
