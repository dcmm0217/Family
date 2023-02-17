package com.wei.create.agent.dongtai;

import com.wei.create.agent.staticd.IGamePlayer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 场景调用类(动态代理)
 *
 * @author huangw
 * @since 2023-02-17
 */
public class Client {

    public static void main(String[] args) {
        // 定义一个游戏玩家
        IGamePlayer player =  new GamePlayer("张三");

        // 定义一个游戏代练
        InvocationHandler handler = new GamePlayIH(player);

        // 获取类的class loader
        ClassLoader cl = player.getClass().getClassLoader();

        // 动态代理产生一个代理者（在编译的时候才知道被谁所代理了，所以称为动态代理）
        IGamePlayer proxy = (IGamePlayer) Proxy.newProxyInstance(cl, new Class[]{IGamePlayer.class}, handler);


        // 代理对象开始帮玩家打游戏
        proxy.login("张三", "password");

        proxy.killBoss();

        proxy.upgrade();

    }
}
