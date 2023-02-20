package com.wei.create.chain.userDesign;

import com.wei.create.chain.userDesign.IWomen;

public abstract class Handler {

    // 父亲处理
    public final static int FATHER_LEVEL_REQUEST = 1;
    // 丈夫处理
    public final static int HUSBAND_LEVEL_REQUEST = 2;
    //儿子处理
    public final static int SON_LEVEL_REQUEST = 3;

    //能处理的级别
    private int level = 0;

    // 责任传递，下一个责任人是谁
    private Handler nextHandler;

    //每个类都要说明一下，自己能处理哪些请求
    public Handler(int level) {
        this.level = level;
    }

    // 一个女性要求逛街时，你要处理这个请求
    public final void handlerMessage(IWomen women) {
        if (women.getType() == this.level) {
            this.response(women);
        } else {
            if (this.nextHandler != null) {
                // 有后续环节，才把请求往后传递
                this.nextHandler.handlerMessage(women);
            } else {
                // 没有后续的人可以来处理这个请求了，直接人工干预把
                System.out.println("------没地方请示了，按不同意处理！！！------");
            }
        }
    }

    /**
     * 如果不是属于你处理的请求，你应该让她找下一个环节的人，如果女儿出嫁了，还想父亲请示是否可以逛街，那父亲就应该告诉女儿，去找你的老公请示
     * @param handler
     */
    public void setNext(Handler handler) {
        this.nextHandler = handler;
    }

    // 有请示 需要给出回应
    protected abstract void response(IWomen women);
}
