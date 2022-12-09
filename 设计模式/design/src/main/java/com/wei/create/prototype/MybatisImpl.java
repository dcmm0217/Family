package com.wei.create.prototype;

import java.util.HashMap;
import java.util.Map;

/**
 * 原型模式的实现
 */
public class MybatisImpl implements IMyBatis {

    // 缓存user，序列化和反序列化-深克隆
    private Map<String, User> userCache = new HashMap<String, User>();

    @Override
    public User getUserFromDb(String username) throws CloneNotSupportedException {
        System.out.println("从数据库中查到：" + username);
        User user = new User();
        user.setAge(18);
        user.setUsername(username);
        // 给缓存中也放入的是克隆的对象
        userCache.put(username, user.clone());
        return user;
    }

    @Override
    public User getUser(String username) throws CloneNotSupportedException {
        User user = null;
        if (userCache.containsKey(username)) {
            // 从缓存中直接拿，脏缓存问题
            // 原型已经拿到，但是不能直接给。
            user = userCache.get(username);
            System.out.println("从缓存中拿的是：" + user);
            user = user.clone();
        } else {
            // 缓存中没有，查询数据库
            user = getUserFromDb(username);
        }
        return user;
    }
}
