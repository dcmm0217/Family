package com.wei.create.agent.staticd;

/**
 * 场景调用类
 *
 * @author huangw
 * @since 2023-02-17
 */
public class Client {

    public static void main(String[] args) {
        // 定义一个游戏玩家
        IGamePlayer player = new GamePlayer("张三");

        // 定义一个游戏代练
        IGamePlayer iGamePlayer = new GamePlayProxy(player);

        // 代练开始帮玩家打游戏
        iGamePlayer.login("张三", "password");

        iGamePlayer.killBoss();

        iGamePlayer.upgrade();

    }
}
