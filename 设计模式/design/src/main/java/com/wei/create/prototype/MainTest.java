package com.wei.create.prototype;

/**
 * 原型模式的测试
 */
public class MainTest {
    public static void main(String[] args) throws CloneNotSupportedException {
        IMyBatis iMyBatis = new MybatisImpl();
        User zhangsan = iMyBatis.getUser("zhangsan");
        System.out.println("第一次拿的:" + zhangsan);
        zhangsan.setUsername("里斯3");
        System.out.println("修改以后的:" + zhangsan);

        User zhangsan1 = iMyBatis.getUser("zhangsan");
        System.out.println("第二次拿的：" + zhangsan1);

        User zhangsan2 = iMyBatis.getUser("zhangsan");
        System.out.println("第三次拿的：" + zhangsan2);
    }
}
