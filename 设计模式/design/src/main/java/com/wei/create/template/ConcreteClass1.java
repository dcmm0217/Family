package com.wei.create.template;

/**
 * 具体模板类1
 *
 * @author huangw
 * @since 2022/12/5 23:05
 */
public class ConcreteClass1 extends AbstractClass {
    @Override
    protected void doSomething() {
        System.out.println("ConcreteClass1->doSomething");
    }

    @Override
    protected void doAnything() {
        System.out.println("ConcreteClass1->doAnything");
    }
}
