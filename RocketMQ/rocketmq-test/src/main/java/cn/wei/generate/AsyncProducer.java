package cn.wei.generate;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.concurrent.TimeUnit;


public class AsyncProducer {
    public static void main(String[] args) throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer("wei-producer");
        producer.setNamesrvAddr("localhost:9876");
        // 指定异步发送失败后不进行重试
        producer.setRetryTimesWhenSendAsyncFailed(0);
        // 指定新创建的Topic的Queue数量为2，默认为4
        producer.setDefaultTopicQueueNums(2);


        producer.start();

        for (int i = 0; i < 100; i++) {
            byte[] bytes = ("hi " + i).getBytes();
            Message message = new Message("topicB","*",bytes);
            // 异步发送，指定回调函数
            try {
                producer.send(message, new SendCallback() {
                    // 当producer接收到mq返回回来的ACK，就会执行此方法
                    public void onSuccess(SendResult sendResult) {
                        System.out.println(sendResult);
                    }

                    public void onException(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        producer.shutdown();
    }
}
