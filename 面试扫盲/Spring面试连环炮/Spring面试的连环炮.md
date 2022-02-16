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



## 7、SpringAOP的底层原理

总：AOP叫面向切面编程，应用场景主要有日志，事务。底层原理是采用动态代理来实现的。

分：bean的创建过程中有一个步骤可以对bean进行扩展实现，aop本身就是一个扩展功能，所以在BeanPostProcessor的后置处理方法中来实现

1、代理对象创建过程（advice，切面，切点）

2、通过jdk或者chlib的方式来生成代理对象

3、在执行方法调用的时候，会调用到生成的字节码文件中，直到回找到DynamicAdvicerdInterceptor类中的intercept方法，从此方法开始执行。

4、根据之前定义好的通知来生成拦截器链

5、从拦截器链中依次获取每一个通知开始进行执行，在执行过程中，为了找到下一个通知是哪个，会有cglibMethodInvocation的对象，找的时候是从-1开始找并且执行的。



## 8、Spring的事务是如何回滚的

这个问题等同于问你

Spring的事务管理是如何实现的？

总：spring的事务是由aop实现的，首先要生成具体的代理对象，然后按照aop的整套流程执行具体的操作逻辑，正常情况下要通过通知来完成核心功能，但是事务不是通过通知来实现的，而是通过一个TransactionInterceptor来实现的，然后调用invoke方法来实现具体的逻辑

分：

1、先做准备工作，解析各个方法上事务相关的属性，根据具体的属性来判断是否开启新事务

2、当需要开启的时候，获取数据库连接，关闭自动提交功能，开启事务

3、执行具体的sql逻辑操作

4、在操作过程中，如果执行失败了，那么会通过completeTransactionAfterThrowing来完成事务的回滚操作，回滚的具体逻辑是通过doRollBack方法实现的，实现的时候也是要先获取连接对象，通过连接对象来回滚

5、如果执行过程中，没有任何意外情况发生，那么通过commitTranscationAfterReturning来完成事务的提交操作，提交的具体逻辑是通过doCommit方法来实现的，实现的时候也要获取连接，通过连接对象来提交。

6、当事务执行完毕后，需要清理相关事务的信息cleanupTransactionInfo来实现。



## 9、谈一下Spring事务传播特性

传播特性有几种？   在TransactionDefinition类中，Spring提供了7种传播属性

- **REQUIRED** :  如果当前已经存在事务，那么加入该事务，如果不存在事务，创建一个事务，这是默认的传播属性值。

- **SUPPORTS**：如果当前已经存在事务，那么加入该事务，否则创建一个所谓的空事务（可以认为无事务执行）

- **MANDATORY**：当前必须存在一个事务，否则抛出异常

- **REQUIRES_NEW**：如果当前存在事务，先把当前事务相关内容封装到一个实体，然后重新创建一个新事务，接受这个实体为参数，用于事务的恢复。

  更直白的说法就是暂停当前事务(当前无事务则不需要)，创建一个新事务。 针对这种情况，两个事务没有依赖关系，可以实现新事务回滚了，但外部事务继续执行

- **NOT_SUPPORTED**：如果当前存在事务，挂起当前事务，然后新的方法在没有事务的环境中执行，没有spring事务的环境下，sql的提交完全依赖于 defaultAutoCommit属性值

- **NEVER**：如果当前存在事务，则抛出异常，否则在无事务环境上执行代码

- **NESTED**：如果当前存在事务，则使用 SavePoint 技术把当前事务状态进行保存，然后底层共用一个连接，当NESTED内部出错的时候，自行回滚到 SavePoint这个状态，只要外部捕获到了异常，就可以继续进行外部的事务提交，而不会受到内嵌业务的干扰，但是，如果外部事务抛出了异常，整个大事务都会回滚。

![image-20220216212321962](https://gitee.com/huangwei0123/image/raw/master/img/image-20220216212321962.png)



----



事务传播特性不会单问，只会出场景题。

比如：某一个事务嵌套另一个事务的时候怎么办？

A方法调用B方法，AB方法都有事务，并且传播特性不相同，那么A如果有异常，B怎么办，B如果有异常，A怎么办？

总：事务的传播特性指的是不同方法的嵌套过程中，事务应该如何处理，是用同一个事务还是不同的事务，当出现异常的时候会回滚还是会提交，两个方法之间的相互影响，在日常工作中，使用的比较多的还是 ==required，required_new，nested==

分：

1、先说事务的不同分类，可以分为三类： 支持当前事务required，不支持当前事务required_new，嵌套事务nested

2、如果外层方法是required , 内层方法是 required ，required_new , nested 

3、如果外层方法是required_new , 内层方法是 required ，required_new , nested 

4、如果外层方法是nested, 内层方法是 required ，required_new , nested 

==核心处理逻辑非常简单：==

判断**内外方法是否是同一个事务**：

​	是：异常统一在外层方法处理

​	不是：内层方法有可能影响到外层方法，但是外层方法不会影响到内层方法。

