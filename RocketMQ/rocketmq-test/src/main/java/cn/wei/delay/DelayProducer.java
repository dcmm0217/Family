package cn.wei.delay;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DelayProducer {
    public static void main(String[] args) throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer("wei-producer");
        producer.setNamesrvAddr("localhost:9876");
        producer.start();

        for (int i = 0; i < 100; i++) {
            byte[] bytes = ("hi "+ i).getBytes();
            Message message = new Message("topicB","*",bytes);
            // 指定消息的延时等级 3-10s
            message.setDelayTimeLevel(3);
            try {
                SendResult sendResult = producer.send(message);
                // 消息被发送的时间
                System.out.print(new SimpleDateFormat("mm:ss").format(new Date()));
                System.out.println("," + sendResult);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                producer.shutdown();
            }
        }
    }
}
