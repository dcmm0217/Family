package cn.wei.generate;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;

public class SomeConsumer {
    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("wei-consumer");
        consumer.setNamesrvAddr("localhost:9876");
        // 设置从第一条消息开始消费
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        // 指定消费的topic和tag
        consumer.subscribe("topicA","*");
        // 指定采用广播模式进行消费，默认为集群模式
        consumer.setMessageModel(MessageModel.BROADCASTING);

        //注册消息监听器
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            // 一旦broker中有了其订阅的消息就会触发该方法的执行
            // 返回值为当前consumer的消费状态
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                // 逐条消费消息
                for (MessageExt messageExt : list) {
                    System.out.println(messageExt);
                }
                // 返回消费状态为成功
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 开启消费者消费
        consumer.start();
        System.out.println("consumer started !");
    }
}
