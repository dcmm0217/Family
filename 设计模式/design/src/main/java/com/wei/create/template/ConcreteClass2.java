package com.wei.create.template;

/**
 * 具体模板类2
 *
 * @author huangw
 * @since  2022/12/5 23:06
 */
public class ConcreteClass2 extends AbstractClass {
    @Override
    protected void doSomething() {
        System.out.println("ConcreteClass2->doSomething");
    }

    @Override
    protected void doAnything() {
        System.out.println("ConcreteClass2->doAnything");
    }
}
