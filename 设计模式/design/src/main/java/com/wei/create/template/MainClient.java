package com.wei.create.template;

/**
 * 场景类
 *
 * @author huangw
 * @since 2022/12/5 23:07
 */
public class MainClient {
    public static void main(String[] args) {
        // 多态的特性，父类的引用执行子类的对象
        AbstractClass concreteClass1 = new ConcreteClass1();
        AbstractClass concreteClass2 = new ConcreteClass2();

        // 调用各抽象类的模板方法
        concreteClass1.templateMethod();
        concreteClass2.templateMethod();
    }
    /*
    注意点：抽象模板类中的基本方法设计为protected类型，符合迪米特法则，不需要暴露的属性或方法尽量不要设计成protected类型，实现类若非必要，尽量不要扩大父类中的权限
    迪米特法则：一个对象应该对其他对象有最少的了解。

    模板方法的模式
    优点：
    1、封装不变部分，扩展可变部分
    2、提取公共代码，便于维护
    3、行为由父类控制，子类实现
    缺点：
    抽象类负责声明最抽象、最一般的事物属性和方法，实现类完成具体的事物属性和方法。
    但是模板方法模式却颠倒了，抽象类定义了部分抽象方法，由子类实现，子类执行的结果影响了父类的结果，也就是子类对父类产生了影响

    -- 一个类应该对自己需要耦合或调用的类知道得最少，你（被耦合或调用的类）的内部是如何复杂都和我没关系，那是你的事情，
    -- 我就知道你提供的这么多public方法，我就调用这么多，其他的我一概不关心。
     */
}
