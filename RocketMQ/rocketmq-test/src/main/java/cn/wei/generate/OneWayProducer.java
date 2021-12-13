package cn.wei.generate;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

public class OneWayProducer {
    public static void main(String[] args) throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer("wei-producer");
        producer.setNamesrvAddr("localhost:9876");

        producer.start();
        for (int i = 0; i < 10; i++) {
            byte[] bytes = ("hi" + i).getBytes();
            Message message = new Message("topicC","*",bytes);
            try {
                // 单向发送
                producer.sendOneway(message);
            } catch (RemotingException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        producer.shutdown();
        System.out.println("producer shutdown !");
    }
}
