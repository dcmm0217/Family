# Spring面试的连环炮

## 1、请谈谈你对SpringIOC的理解

**IOC：控制反转**，原来我们使用对象的时候是由使用者控制者控制的，有了Spring以后，可以将整个对象交给容器来帮我们进行管理。（IOC其实是一种理论思想），谈到IOC又不是不提及到它的实现方式DI（依赖注入），Spring中所有的bean都是通过反射生成的。

用一句话形容就是==你不要打给我了，我会主动打给你==

**DI：依赖注入**，将对应的属性注入到具体的对象中，经常使用的注解有@autowired，@resource，populateBean方法来完成属性注入。

**容器：存储对象**，使用Map结构存储对象， 在Sping中储存对象的时候一般有三级缓存，`singletonObjects`存放完整的对象；`earlySingletonObjects`存放半成品对象，`singletonFactory`用来存放lambda表达式和对象名称的映射，整个bean的生命周期，从创建到使用到销毁，各个环节都是由容器来帮我们控制的。



## 2、简单描述下Spring Bean的生命周期

Spring容器帮助我们去管理对象，从对象的产生到销毁的环节都由容器来控制，其中主要包含实例化和初始化两个关键节点，当然在整个过程中会有一些扩展的存点。 下面来详细描述各个环节和步骤：

1、实例化bean对象，通过反射的方式来生成，在源码里有一个createBeanInstance方法是专门生成对象的。(先去获取构造器反射`clazz.getDeclaredConstructor()`，然后通过构造器的反射对象去`ctor.newInstance()`)

2、当bean对象创建完成之后，对象的属性值都是默认值，所以要给bean填充属性，通过populateBean方法来完成对象属性的填充，==中间会设计到循环依赖的问题==。后面详说。

3、向bean对象中设置容器属性，会调用`invokeAwareMethods`方法来将容器对象设置到具体的bean对象中

4、调用`BeanPostProcessor`中的前置处理方法来进行bean对象的扩展工作，比如：`ApplicationContextPostProcessor`，`EmbeddValueResolver`等对象

5、调用`invokeInitMethods`方法来完成初始化方法的调用，在此方法处理过程中，需要判断当前bean对象是否实现了InitializingBean接口，如果实现了调用afterPropertiesSet方法来最后设置bean对象。

6、调用`BeanPostProcessor`的后置处理方法，完成对Bean对象的后置处理工作，AOP就是在此处实现的，实现的接口实现名字叫做`AbstractAutoProxyCeator`

7、获取到完整对象，通过getBean的方式进行对象的获取和使用

8、当对象使用完成后，容器在关闭的时候，会销毁对象，首先会判断是否实现了`DispoableBean`接口，然后去调用`destoryMethod`方法。

==看图说话：==

![image-20220213231006305](https://gitee.com/huangwei0123/image/raw/master/img/image-20220213231006305.png)





## 3、BeanFactory和FactoryBean的区别

1、BeanFactory和FactoryBean都可以用来创建对象，只不过创建的方式和流程不同。

2、当使用BeanFactory的时候，必须要严格的遵守Bean的生命周期，经过一系列繁杂的步骤之后才可以创建出单例对象，是流水线式的创建过程。

3、而FactroyBean式用户可以自定义bean对象的创建过程，不需要按照bean的生命周期来创建，在此接口中包含了三个方法：

- isSingleton：判断是否式单例对象
- getObjectType：获取对象的类型
- getObject：在此方法中可以自己创建对象，使用new的方式或者使用代理的方式都可以，用户可以按照自己的需要去随意创建对象，在很多框架继承的时候都会实现FactoryBean接口，比如feign。



## 4、Sping中用到哪些设计模式

==单例模式==：spring中bean都是单例

==工厂模式==：BeanFactory

模板方法：PostProcessorBeanFactory，onRefresh

观察者模式：listener，event，multicast

适配器模式：Adapter

装饰器模式：BeanWrapper

责任链模式：使用aop的时候会有一个责任链模式

==代理模式==：aop动态代理

建造者模式：builder

==策略模式==：XmlBeanDefinitionReader，PropertiesBeanDefinitionReader



## 5、applicationContext和BeanFactory的区别

BeanFactory式访问Spring容器的根接口，里面只是提供了某些基本方法的约束和规范，

为了满足更多的需求，applicationContext实现了此接口，并在此接口的基础上做了某些扩展的功能，提供了更加丰富的api调用。

一般我们使用的时候用applicationContext更多



## 6、谈谈你对Spring循环依赖的理解
