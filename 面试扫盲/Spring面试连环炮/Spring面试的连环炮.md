# Spring面试的连环炮

## 1、请谈谈你对SpringIOC的理解

**IOC：控制反转**，原来我们使用对象的时候是由使用者控制者控制的，有了Spring以后，可以将整个对象交给容器来帮我们进行管理。（IOC其实是一种理论思想），谈到IOC又不是不提及到它的实现方式DI（依赖注入），Spring中所有的bean都是通过反射生成的。

用一句话形容就是==你不要打给我了，我会主动打给你==

**DI：依赖注入**，将对应的属性注入到具体的对象中，注入类型有两种，一种是byType，一种是byName，在我们经常使用的注解有@Autowired是通过byType来实现注入的，需要注入相同类型的的对象的话，还要使用@Qualifier来区分，当@Resource不设置任何值时，isDefaultName会为true，**当对应字段名称的bean或者BeanDefinition已存在时会走byName的形式，否则走byType的形式**；populateBean（是对对象属性的一个填充）方法来完成属性注入。

**容器：存储对象**，使用Map结构存储对象， 在Sping中储存对象的时候一般有三级缓存，`singletonObjects`存放完整的对象（一级缓存）；`earlySingletonObjects`存放半成品对象（二级缓存），`singletonFactories`用来存放lambda表达式和对象名称的映射（三级缓存），整个bean的生命周期，从创建到使用到销毁，各个环节都是由容器来帮我们控制的。



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

#### 什么是循环依赖?

![image-20220215172733058](https://gitee.com/huangwei0123/image/raw/master/img/image-20220215172733058.png)

简单来说就是A依赖B，B依赖A

Spring中bean对象的创建都要经历实例化和初始化（属性填充）的过程，通过将对象的状态分开，存在半成品和成品对象的方式，来分别进行初始化和实例化，成品和半成品在存储的时候需要分不同的缓存进行存储。

![image-20220215173037087](https://gitee.com/huangwei0123/image/raw/master/img/image-20220215173037087.png)

==当持有了某个对象的引用之后，能否在后续的某个步骤中对该对象进行赋值操作？==

**可以。**利用这点，我们可以将半成品对象放入容器，然后再逆向的去赋值。

![image-20220215173821740](https://gitee.com/huangwei0123/image/raw/master/img/image-20220215173821740.png)



#### 只有一级缓存行不行？

不行，会把成品状态的bean对象和半成品对象的bean对象放到一起，而半成品对象是无法暴露给外部使用的，所以要将成品和半成品分开，一级缓存放成品对象，二级缓存放半成品对象。

#### 只有二级缓存行不行？

如果整个应用程序中不涉及aop的存在，那么二级缓存足以解决循环依赖的问题，如果aop中存在了循环依赖，那么就必须使用三级缓存才能解决。

#### 为什么需要三级缓存？

三级缓存的value类型是ObjectFactory，是一个函数式接口，不能直接进行调用，只有在调用getObject方法的时候才回去调用里面存储的lambda表达式，存在的意义就是保证整个容器在允许过程中同名的bean对象只能有一个。

如果一个对象被代理，或者说需要生成代理对象，那么要不要先生成一个原始对象？   **是需要的**

当创建出代理对象之后，会同时存在代理对象和普通对象，此时我该用哪一个?

程序是死的，他怎么知道先用谁后用谁呢？

当需要代理对象的时候，或者说代理对象生成的时候，必须要覆盖原始对象，也就是说整个容器中，有且仅有一个同名的bean对象。

在实际调用过程中，是没有办法来确定什么时候对象需要被调用，因此当某一个对象被调用的时候，优先判断当前对象是否需要被代理，类似于回调机制，当获取对象之后，根据传入的lambda表达式来确认返回的是哪一个确定的对象，如果条件符合，返回代理对象，如何不符合，返回原始对象。

