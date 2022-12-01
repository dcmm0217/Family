package com.wei.create.factroy;

/**
 * 工程方法模式 的工厂类  专门用来创建人
 *
 * @author huangw
 * @since 2022-12-01 14:52:12
 */
public class HumanFactory extends AbstractHumanFactory {

    @Override
    public <T extends Human> T createHuman(Class<T> c) {
        // 定义一个人种
        Human human = null;
        try {
            // 通过反射生成对象
            human = (T) Class.forName(c.getName()).newInstance();
        } catch (Exception e) {
            System.out.println("人种生成错误");
        }
        return (T) human;
    }
}
