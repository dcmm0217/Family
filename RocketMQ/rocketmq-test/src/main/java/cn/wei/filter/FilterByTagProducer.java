package cn.wei.filter;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

public class FilterByTagProducer {
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("wei-producer");
        producer.setNamesrvAddr("localhost:9876");
        producer.start();
        String[] tags = {"TAGA","TAGB","TAGC"};

        for (int i = 0; i < 3; i++) {
            byte[] bytes = ("Hi" + i).getBytes();
            String tag = tags[i%tags.length];
            Message message = new Message("myTopic",tag,bytes);
            SendResult sendResult = producer.send(message);
            System.out.println(sendResult);
        }
        producer.shutdown();
    }
}
