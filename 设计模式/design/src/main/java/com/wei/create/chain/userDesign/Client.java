package com.wei.create.chain.userDesign;

import com.wei.create.chain.userDesign.Father;
import com.wei.create.chain.userDesign.Husband;
import com.wei.create.chain.userDesign.IWomen;
import com.wei.create.chain.userDesign.Son;
import com.wei.create.chain.userDesign.Women;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Client {

    public static void main(String[] args) {
        Random random = new Random();
        List<IWomen> womenList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            womenList.add(new Women(random.nextInt(4), "我要出去逛街！"));
        }
        // 定义3个请求对象
        Handler father = new Father();
        Handler son = new Son();
        Handler husband = new Husband();
        // 设置请求顺序
        father.setNext(husband);
        husband.setNext(son);
        for (IWomen women : womenList) {
            father.handlerMessage(women);
        }
    }
}
