package com.wei.create.agent.staticd;

import lombok.Data;

/**
 * 被代理人（明星）
 *
 * @author huangw
 * @since 2023-02-17
 */
@Data
public class GamePlayer implements IGamePlayer {

    private String name;

    public GamePlayer() {
    }

    public GamePlayer(String name) {
        this.name = name;
    }

    @Override
    public void login(String user, String password) {
        System.out.println("登录名为" + user + "的用户" + this.name + "登录成功!");
    }

    @Override
    public void killBoss() {
        System.out.println(this.name + "在打怪！");
    }

    @Override
    public void upgrade() {
        System.out.println(this.name + "又升了一级！");
    }
}
