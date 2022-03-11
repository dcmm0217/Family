# 第2节-java类加载机制

## 1、类加载的过程（生命周期）

==面试题：说说类加载过程？==

切入点：1、谁需要加载  2、加载的步骤

1. 在Java中数据类型分为基本数据类型和引用数据类型。==基本数据类型由JVM预先定义，引用数据类型则需要进行类的加载==。

从class文件到加载内存中的类，到类卸载出内存为止。它的整个生命周期包括7个阶段

![image-20220308223020159](https://gitee.com/huangwei0123/image/raw/master/img/image-20220308223020159.png)

#### **过程1：类的装载**

所谓装载，简而言之就是将Java类的字节码文件加载到机器内存中，并在内存中构建出Java类的原型----类模板对象。

类模板1.8元空间（7永久代）

加载时类加载过程的第一个阶段，在加载阶段，虚拟机需要完成以下三件事情:

- 通过一个类的全名，来获取其定义的二进制字节流。
- 解析类的二进制数据流为方法区内的数据结构（Java类模型）
- 创建java.lang.class类的实例，表示该类型。作为方法区这个类的各种数据的访问入口

二进制流的获取方式：

- 虚拟机通过本地文件系统读入一个class后缀的文件（**常用**）
- 读入jar、zip等数据包，提取class文件
- 网络下载.class文件

class实例的位置在哪？

类将.class文件加载至元空间后，会在==堆==中创建一个Java.lang.class对象，用来封装类位于方法区内的数据结构，该class类对象是在加载类的过程中创建的，每个类都对应有一个class类型的对象。

![image-20220308224447405](https://gitee.com/huangwei0123/image/raw/master/img/image-20220308224447405.png)

#### **过程2：链接**（包含验证，准备，解析）

##### 链接过程之验证阶段（verification）

当类加载到系统后，就开始链接操作，验证是链接操作的第一步。

目的是为了确保Class文件的字节流中包含的信息符合当前虚拟机的要求，并且不会危害虚拟机自身的安全。

![image-20220309203827375](https://gitee.com/huangwei0123/image/raw/master/img/image-20220309203827375.png)

`文件格式验证`: 验证字节流是否符合Class文件格式的规范；例如: 是否以`0xCAFEBABE`开头、主次版本号是否在当前虚拟机的处理范围之内、常量池中的常量是否有不被支持的类型。

`元数据验证`: 对字节码描述的信息进行语义分析(注意: 对比`javac`编译阶段的语义分析)，以保证其描述的信息符合Java语言规范的要求；例如: 这个类是否有父类，除了`java.lang.Object`之外。

`字节码验证`: 通过数据流和控制流分析，确定程序语义是合法的、符合逻辑的。

`符号引用验证`: 确保解析动作能正确执行。

> 验证阶段是非常重要的，但不是必须的，它对程序运行期没有影响，*如果所引用的类经过反复验证，那么可以考虑采用`-Xverifynone`参数来关闭大部分的类验证措施，以缩短虚拟机类加载的时间*

##### 链接过程之准备阶段（PreParation）

**简而言之，为类的静态变量分配内存，并将其初始化为默认值。**

- 这时候进行内存分配的仅包括类变量(`static`)，而不包括实例变量，**实例变量会在对象实例化时随着对象一块分配在Java堆中**
- 这里所设置的初始值通常情况下是数据类型默认的零值(如：`0`、`oL`、`null`、`false`等)，而不是被在Java代码中显式地赋予的值。（==显式赋值是在初始化阶段==）

##### 链接过程之解析（Resolution）

**简言之，将类、接口、字段和方法的符号引用转为直接引用。**

解析阶段是虚拟机将常量池内的符号引用替换为直接引用的过程，解析动作主要针对`类`或`接口`、`字段`、`类方法`、`接口方法`、`方法类型`、`方法句柄`和`调用点`限定符7类符号引用进行。

符号引用就是一组符号来描述目标，可以是任何字面量。

`直接引用`就是直接指向目标的指针、相对偏移量或一个间接定位到目标的句柄。

#### 过程3：初始化阶段

初始化阶段，简言之，**为类的静态变量赋予正确的初始值。**（到了初始化阶段才真正开始执行类中定义的Java程序代码）。

> 子类加载前一定先会加载父类吗？

**JVM初始化步骤**

- 假如这个类还没有被加载和连接，则程序先加载并连接该类
- 假如该类的直接父类还没有被初始化，则先初始化其直接父类
- 假如类中有初始化语句，则系统依次执行这些初始化语句

==初始化阶段的重要工作时执行类的初始化方法：<clinit>（）方法==

JVM负责对类进行初始化，主要对类变量进行初始化。在Java中对类变量进行初始值设定有两种方式:

- 声明类变量是指定初始值
- 使用静态代码块为类变量指定初始值

只有在给类中的**static的变量显式赋值或在静态代码块中赋值**了。才会生成此方法。<clinit>（）

<init>（）方法一定会出现在Class的method表中。

总结：使用static + final 修饰的成员变量，称为：全局常量

什么时候在链接阶段的准备环节：==给此全局常量附的值是字面量或常量。不涉及到方法或构造器的调用。==除此之外，都是在初始化环节赋值的。

> <clinit>的调用会死锁吗？

对于clinit方法调用，也就是类的初始化，虚拟机会在内部确保其多线程环境中的安全性。

虚拟机会保障一个类的<clinit>方法在多线程环境中被正确的加锁、同步。如果多个线程同时区初始化一个类，那么只有一个线程回去执行这个类的<clinit>方法，其他线程都需要阻塞。正因为<clinit>是带锁线程安全的。因此，如果在一个类<clinit>方法中耗时很长的操作，就可能造成多个线程阻塞，引发**死锁**。

> 类加载的时机？

**类初始化时机**: 

只有当对类的主动使用的时候才会导致类的初始化，类的**主动使用**包括以下六种:

- 创建类的实例，也就是new的方式

- 访问某个类或接口的静态变量，或者对该静态变量赋值

- 调用类的静态方法

- 反射(如Class.forName("com.pdai.jvm.Test"))

- 初始化某个类的子类，则其父类也会被初始化

- Java虚拟机启动时被标明为启动类的类(包含main方法的那个类)，直接使用java.exe命令来运行某个主类

**被动使用：**被动使用不会引起类的初始化。意味着没有<clinit>的调用

- 当访问一个静态字段时，只有真正声明这个字段的类才会被初始化（通过子类引用父类的静态变量，不会导致子类初始化）
- 通过数组定义引用，不会触发此类的初始化
- 引用常量不会触发此类或接口的初始化
- 调用ClassLoader类的loadClass（）方法加载一个列，并不是对类的主动使用，不会导致类的初始化。

#### 过程4：类的使用（Using）

开发人员可以在程序中访问和调用它的静态类成员信息（比如：静态字段、静态方法）或者使用new关键字为其创建对象实例。

类访问方法区内的数据结构的接口， 对象是Heap区的数据。

#### 过程5：类的卸载（Unloading）

类、类的加载器、类的实例之间的关系

![image-20220309220023503](https://gitee.com/huangwei0123/image/raw/master/img/image-20220309220023503.png)

**Java虚拟机将结束生命周期的几种情况**

- 执行了System.exit()方法
- 程序正常执行结束
- 程序在执行过程中遇到了异常或错误而异常终止
- 由于操作系统出现错误而导致Java虚拟机进程终止

## 2、类的加载器

类加载器时JVM执行类加载机制的前提。

> ClassLoader的作用：

ClassLoader是Java核心组件，所有的Class都是由ClassLoader进行加载的。

CLassLoader负责通过各种方式将Class信息的二进制数据流读入JVM内部，转换为一个与目标类对应的java.lang.Class对象实例。然后交给Java虚拟机进行链接、初始化等操作

因此，ClassLoader在整个装载阶段，只能影响到类的加载，而无法通过ClassLoader区改变类的链接和初始化行为。至于它是否可以运行，则由`Execution Engine`(执行引擎)决定

> 类的加载分类：显示加载和隐式加载

class文件的显式加载与隐式加载的方式是指JVM加载class文件到内存的方式。

- 显示加载：指的是代码中通过调用ClassLoader加载class对象，如直接使用Class.forName(name)或this.getClass().getClassLoader().loadClass()加载class对象
- 隐式加载：则是不直接在代码中调用ClassLoader的方法加载class对象，而是通过虚拟机自动加载到内存中，如在某个类的class文件时，该类的Class文件中引用了另一个类的对象，此时额外引用的类将通过JVM自动加载到内存中。

> 类加载机制的必要性

- 避免在开发中遇到ClassNotFoundException异常或NoClassDefFoundError异常时，手足无措。**了解类加载器的类加载机制才能在出现异常的时候快速定位问题**
- 需要支持类的动态加载或需要对编译后的字节码文件进行加密操作时，就需要与类加载器打交道了
- 开发人员可以在程序中**自定义加载器**来重新定义类的加载规则，以便实现一些自定义的处理逻辑。

#### 2.1 类加载器的分类

![image-20220310215424731](https://gitee.com/huangwei0123/image/raw/master/img/image-20220310215424731.png)

注意: 这里父类加载器并不是通过继承关系来实现的，而是采用组合实现的。

站在Java虚拟机的角度来讲，只存在两种不同的类加载器: `启动类加载器和其他类加载器`。

**启动类加载器:** 它使用C++实现(这里仅限于`Hotspot`，也就是JDK1.5之后默认的虚拟机，有很多其他的虚拟机是用Java语言实现的)，是虚拟机自身的一部分

**其他的类加载器:** 这些类加载器都由Java语言实现，独立于虚拟机之外，并且全部继承自抽象类`java.lang.ClassLoader`，这些类加载器需要由启动类加载器加载到内存中之后才能去加载其他的类。

**站在Java开发人员的角度来看，类加载器可以大致划分为以下三类** :

`启动类加载器`：

- 这个类加载器是使用C/C++语言实现的，嵌套在JVM内部
- 它用来加载Java的核心库（JAVA_HOME/jar/lib/rt.jaar或sun.boot.class.path路径下的内容）。用于提供JVM自身需要的类
- 并不继承自java.lang.ClassLoader，没有父类加载器
- 出于安全考虑，BootStrap启动类加载器只加载包名为java、javax、sun等开头的类
- 加载扩展类和应用程序加载器，并指定为它们的父类加载器

`扩展类加载器（Extension ClassLoader）`

- 由java语言编写，由sun.misc.Launcher$ExtClassLoader实现
- 继承于ClassLoader类
- 父类加载器为启动类加载器
- 从java.ext.dirs系统属性所指定的目录中加载类库，或从JDK的安装目录jre/lib/ext子目录加载类库。如果用户创建JAR放在此目录下，也会自动由扩展类加载器加载。

`应用程序加载器（系统类加载器）APPClassLoader`

- 由java语言编写，由sun.misc.Launcher$APPClassLoader实现
- 继承于ClassLoader类
- 父类加载器为扩展类加载器
- 它负责加载环境变量classpath或系统属性java.class.path指定路径下的类库
- **应用程序中的类加载器默认系统类加载器**
- 它是用户自定义类加载器的默认父加载器
- 通过ClassLoader的getSystemClassLoader()方法可以获取到该类加载器

#### 2.2 双亲委派机制

- 当AppClassLoader加载一个class时，它首先不会自己去尝试加载这个类，而是把类加载请求委派给父类加载器ExtClassLoader去完成。

- 当ExtClassLoader加载一个class时，它首先也不会自己去尝试加载这个类，而是把类加载请求委派给BootStrapClassLoader去完成。

- 如果BootStrapClassLoader加载失败(例如在$JAVA_HOME/jre/lib里未查找到该class)，会使用ExtClassLoader来尝试加载；

- 若ExtClassLoader也加载失败，则会使用AppClassLoader来加载，如果AppClassLoader也加载失败，则会报出异常ClassNotFoundException。

**双亲委派的优势：**

- 避免类的重复加载，确保一个类的全局唯一性

==Java类随着它的类加载器一起具备了一种带有优先级的层级关系，通过这种层级关系可以避免类的重复加载，==当父亲已经加载了该类时，就没必要子ClassLoader再去加载一次。

- 保护程序安全，防止核心API被随意窜改。
- 系统类防止内存中出现多份同样的字节码
- 保证Java程序安全稳定运行

**双亲委派的弊端：**

- 检查类是否加载的过程是单向的，顶层的ClassLoader无法访问底层的ClassLoader所加载的类
- 通常情况下，启动类加载器中的类为系统核心类，包括一些重要的接口，而在应用类加载器中，为应用类。==按照这种模式，应用类访问系统类自然是没有问题，但是系统类访问应用类就会出现问题。==

比如：在系统类中提供了一个接口，该接口需要在应用类中得以实现，该接口还绑定一个工厂方法，用于创建该接口的实例，而接口和工厂都在启动类加载器中。这时，就会出现该工厂方法无法创建由应用类加载器加载的应用实例的问题。

**结论：**

由于Java虚拟机规范并没有明确要求类加载器机制一定要使用双亲委派模型，只是建议使用这种方式而已。

**双亲委派代码实现**

```java
public Class<?> loadClass(String name)throws ClassNotFoundException {
            return loadClass(name, false);
    }
    protected synchronized Class<?> loadClass(String name, boolean resolve)throws ClassNotFoundException {
            // 首先判断该类型是否已经被加载
            Class c = findLoadedClass(name);
            if (c == null) {
                //如果没有被加载，就委托给父类加载或者委派给启动类加载器加载
                try {
                    if (parent != null) {
                         //如果存在父类加载器，就委派给父类加载器加载
                        c = parent.loadClass(name, false);
                    } else {
                    //如果不存在父类加载器，就检查是否是由启动类加载器加载的类，通过调用本地方法native Class findBootstrapClass(String name)
                        c = findBootstrapClass0(name);
                    }
                } catch (ClassNotFoundException e) {
                 // 如果父类加载器和启动类加载器都不能完成加载任务，才调用自身的加载功能
                    c = findClass(name);
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }

```

**破坏双亲委派机制及其举例：**



#### 2.3 自定义类加载器

为什么需要自定义类加载器？

- 隔离加载类

  在某些框架内进行中间件与应用模块隔离，把类加载到不同的环境中。比如阿里某容器框架通过自定义加载器加载确保应用中依赖的jar包不会影响到中间件运行时使用的jar包。	

- 修改类加载的方式

类加载模型并非强制，除了bootstrap以外，其他加载并非一定要引入，可以根据实际情况进行动态加载

- 扩展加载源

比如从数据库、网络、甚至是电视机机顶盒加载

- 防止源码泄露

Java代码容易被编译和篡改，可以进行编译加密。那么也需要自定义，还原加密字节码。

```java
package com.pdai.jvm.classloader;
import java.io.*;

public class MyClassLoader extends ClassLoader {

    private String root;

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classData = loadClassData(name);
        if (classData == null) {
            throw new ClassNotFoundException();
        } else {
            return defineClass(name, classData, 0, classData.length);
        }
    }

    private byte[] loadClassData(String className) {
        String fileName = root + File.separatorChar
                + className.replace('.', File.separatorChar) + ".class";
        try {
            InputStream ins = new FileInputStream(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length = 0;
            while ((length = ins.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public static void main(String[] args)  {

        MyClassLoader classLoader = new MyClassLoader();
        classLoader.setRoot("D:\\temp");

        Class<?> testClass = null;
        try {
            testClass = classLoader.loadClass("com.pdai.jvm.classloader.Test2");
            Object object = testClass.newInstance();
            System.out.println(object.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

```

自定义类加载器的核心在于对字节码文件的获取，如果是加密的字节码则需要在该类中对文件进行解密。由于这里只是演示，我并未对class文件进行加密，因此没有解密的过程。

**这里有几点需要注意** :

1、这里传递的文件名需要是类的全限定性名称，即`com.pdai.jvm.classloader.Test2`格式的，因为 defineClass 方法是按这种格式进行处理的

2、最好不要重写loadClass方法，因为这样容易破坏双亲委托模式。

3、这类Test 类本身可以被 AppClassLoader 类加载，因此我们不能把com/pdai/jvm/classloader/Test2.class 放在类路径下。否则，由于双亲委托机制的存在，会直接导致该类由 AppClassLoader 加载，而不会通过我们自定义类加载器来加载。
