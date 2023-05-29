package com.design.yes;

import com.design.no.LotteryResult;
import com.design.yes.event.EventManager;
import com.design.yes.event.listener.MQEventListener;
import com.design.yes.event.listener.MessageEventListener;

public abstract class LotteryService {

    private EventManager eventManager;


    public LotteryService() {
        eventManager = new EventManager(EventManager.EventType.MQ, EventManager.EventType.Message);
        eventManager.subscribe(EventManager.EventType.MQ, new MQEventListener());
        eventManager.subscribe(EventManager.EventType.Message, new MessageEventListener());
    }


    public LotteryResult draw(String uId) {
        LotteryResult lotteryResult = doDraw(uId);
        // 需要什么通知就给调用什么方法
        eventManager.notify(EventManager.EventType.Message, lotteryResult);
        eventManager.notify(EventManager.EventType.MQ, lotteryResult);
        return lotteryResult;
    }


    /**
     * 抽象的摇号接口
     *
     * @param uId
     * @return
     */
    protected abstract LotteryResult doDraw(String uId);
}
