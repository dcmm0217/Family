package cn.wei.transaction;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 工行事务消息监听器
 */
public class ICBCTransactionListener implements TransactionListener {

    // 回调操作方法
    // 消息预提交以后会执行这个方法，完成本地事务
    @Override
    public LocalTransactionState executeLocalTransaction(Message message, Object o) {
        System.out.println("预提交消息成功: " + message);
        // 假设接收到TagA的消息就表示扣款成功，TagB的消息就是表示失败，TagC就是未知
        if (StringUtils.equals("TAGA",message.getTags())){
            return LocalTransactionState.COMMIT_MESSAGE;
        }else if (StringUtils.equals("TAGB",message.getTags())){
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }else {
            return LocalTransactionState.UNKNOW;
        }
    }

    // 消息回查方法
    // 引发消息回查的原因最常见的有两个：
    // 1) 回调操作返回UNKNWON
    // 2) 事务协调者（TC） 也就是Broker 没有收到 事务管理器（TM） 也就是Producer 的最终全局指令
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
        System.out.println("执行消息回查:" + messageExt.getTags());
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
