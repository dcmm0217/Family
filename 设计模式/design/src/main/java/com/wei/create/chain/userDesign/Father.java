package com.wei.create.chain.userDesign;

import com.wei.create.chain.userDesign.IWomen;

public class Father extends Handler {

    // 父亲只能处理女儿的请求
    public Father() {
        super(Handler.FATHER_LEVEL_REQUEST);
    }

    @Override
    protected void response(IWomen women) {
        System.out.println("--------女儿向父亲请示--------");
        System.out.println(women.getRequest());
        System.out.println("父亲的回答是：同意！\n");
    }
}
