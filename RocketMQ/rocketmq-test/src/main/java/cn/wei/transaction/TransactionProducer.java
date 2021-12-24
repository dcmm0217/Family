package cn.wei.transaction;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.concurrent.*;

/**
 * 事务消息生产者
 */
public class TransactionProducer {
    public static void main(String[] args) throws MQClientException {
        TransactionMQProducer producer = new TransactionMQProducer("wei-producer");
        producer.setNamesrvAddr("localhost:9876");
        ExecutorService executor = new ThreadPoolExecutor(2, 5, 10L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "client-transaction-mythread");
                return thread;
            }
        });
        // 给事务消息生产者设置线程池
        producer.setExecutorService(executor);

        // 给生产者添加线程监听器
        producer.setTransactionListener(new ICBCTransactionListener());
        producer.start();

        String[] tags = {"TAGA", "TAGB", "TAGC"};
        for (int i = 0; i < 3; i++) {
            byte[] bytes = (("Hi" + i)).getBytes();
            Message message = new Message("TTopic", tags[i], bytes);
            // 发送事务消息
            // 第二个参数用于指定在执行本地事务时要使用的业务参数
            TransactionSendResult transactionSendResult = producer.sendMessageInTransaction(message,null);
            System.out.println("发送结果: " + transactionSendResult.getSendStatus());
        }
    }
}
