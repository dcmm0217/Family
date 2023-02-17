package com.wei.create.agent.staticd;

import lombok.Data;

/**
 * 代理类（经纪人）
 *
 * @author huangw
 * @since 2023-02-17
 */
@Data
public class GamePlayProxy implements IGamePlayer {

    private IGamePlayer gamePlayer;

    public GamePlayProxy() {
    }

    public GamePlayProxy(IGamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    @Override
    public void login(String user, String password) {
        this.gamePlayer.login(user, password);
    }

    @Override
    public void killBoss() {
        this.gamePlayer.killBoss();
    }

    @Override
    public void upgrade() {
        this.gamePlayer.upgrade();
    }
}
