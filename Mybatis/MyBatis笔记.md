# MyBatis笔记

## 1、什么是MyBatis

#### 1.1 环境说明

- jdk 8 +

- MySQL 5.7.19

- maven-3.6.1
- IDEA

#### 1.2 学习前需要掌握

- JDBC
- MySQL
- Java 基础
- Maven
- Junit

#### 1.3 什么是MyBatis

- MyBatis 是一款优秀的**持久层框架**
- MyBatis 避免了几乎所有的 JDBC 代码和手动设置参数以及获取结果集的过程
- MyBatis 可以使用简单的 XML 或注解来配置和映射原生信息，将接口和 Java 的 实体类 【Plain Old Java Objects,普通的 Java对象】映射成数据库中的记录。
- MyBatis 本是apache的一个开源项目ibatis, 2010年这个项目由apache 迁移到了google code，并且改名为MyBatis 。
- 2013年11月迁移到**Github** .
- Mybatis官方文档 : http://www.mybatis.org/mybatis-3/zh/index.html
- GitHub : https://github.com/mybatis/mybatis-3

#### 1.4 持久化

持久化是将程序数据在持久状态和瞬时状态间转换的机制。

- 即把数据（如内存中的对象）保存到可永久保存的存储设备中（如磁盘）。持久化的主要应用是将内存中的对象存储在数据库中，或者存储在磁盘文件中、XML数据文件中等等。
- JDBC就是一种持久化机制。文件IO也是一种持久化机制。
- 在生活中 : 将鲜肉冷藏，吃的时候再解冻的方法也是。将水果做成罐头的方法也是。

**为什么需要持久化服务呢？那是由于内存本身的缺陷引起的**

- 内存断电后数据会丢失，但有一些对象是无论如何都不能丢失的，比如银行账号等，遗憾的是，人们还无法保证内存永不掉电。
- 内存过于昂贵，与硬盘、光盘等外存相比，内存的价格要高2~3个数量级，而且维持成本也高，至少需要一直供电吧。所以即使对象不需要永久保存，也会因为内存的容量限制不能一直呆在内存中，需要持久化来缓存到外存。

#### 1.5 持久层

**什么是持久层？**

- 完成持久化工作的代码块 .  ---->  dao层 【DAO (Data Access Object)  数据访问对象】

- 大多数情况下特别是企业级应用，数据持久化往往也就意味着将内存中的数据保存到磁盘上加以固化，而持久化的实现过程则大多通过各种**关系数据库**来完成。

- 不过这里有一个字需要特别强调，也就是所谓的“层”。对于应用系统而言，数据持久功能大多是必不可少的组成部分。也就是说，我们的系统中，已经天然的具备了“持久层”概念？也许是，但也许实际情况并非如此。之所以要独立出一个“持久层”的概念,而不是“持久模块”，“持久单元”，也就意味着，我们的系统架构中，应该有一个相对独立的逻辑层面，专注于数据持久化逻辑的实现.

- 与系统其他部分相对而言，这个层面应该具有一个较为清晰和严格的逻辑边界。【说白了就是用来操作数据库存在的！】

  

#### 1.6 为什么需要Mybatis

- Mybatis就是帮助程序猿将数据存入数据库中 , 和从数据库中取数据 .

- 传统的jdbc操作 , 有很多重复代码块 .比如 : 数据取出时的封装 , 数据库的建立连接等等... , 通过框架可以减少重复代码,提高开发效率 .

- MyBatis 是一个半自动化的**ORM框架 (Object Relationship Mapping) -->对象关系映射**

- 所有的事情，不用Mybatis依旧可以做到，只是用了它，所有实现会更加简单！**技术没有高低之分，只有使用这个技术的人有高低之别**

- MyBatis的优点

- - 简单易学：本身就很小且简单。没有任何第三方依赖，最简单安装只要两个jar文件+配置几个sql映射文件就可以了，易于学习，易于使用，通过文档和源代码，可以比较完全的掌握它的设计思路和实现。
  - 灵活：mybatis不会对应用程序或者数据库的现有设计强加任何影响。sql写在xml里，便于统一管理和优化。通过sql语句可以满足操作数据库的所有需求。
  - 解除sql与程序代码的耦合：通过提供DAO层，将业务逻辑和数据访问逻辑分离，使系统的设计更清晰，更易维护，更易单元测试。sql和代码的分离，提高了可维护性。
  - 提供xml标签，支持编写动态sql。
  - .......

- 最重要的一点，使用的人多！公司需要！





## 2、第一个MyBatis程序

**思路流程：搭建环境-->导入Mybatis--->编写代码--->测试**

#### 2.1 搭建实验数据库

#### 2.2 导入MyBatis相关 jar 包

- GitHub上找

```xml
<dependency>
   <groupId>org.mybatis</groupId>
   <artifactId>mybatis</artifactId>
   <version>3.5.2</version>
</dependency>
<dependency>
   <groupId>mysql</groupId>
   <artifactId>mysql-connector-java</artifactId>
   <version>5.1.47</version>
</dependency>
```

#### 2.3 编写MyBatis核心配置文件

- 编写MyBatis核心配置文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
       PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
       "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
   <environments default="development">
       <environment id="development">
           <transactionManager type="JDBC"/>
           <dataSource type="POOLED">
               <property name="driver" value="com.mysql.jdbc.Driver"/>
               <property name="url" value="jdbc:mysql://localhost:3306/mybatis?useSSL=true&amp;useUnicode=true&amp;characterEncoding=utf8"/>
               <property name="username" value="root"/>
               <property name="password" value="root"/>
           </dataSource>
       </environment>
   </environments>
   <mappers>
       <mapper resource="com/stuy/dao/userMapper.xml"/>
   </mappers>
</configuration>
```

#### 2.4 编写MyBatis工具类

- 查看帮助文档

```java
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import java.io.IOException;
import java.io.InputStream;

public class MybatisUtils {

   private static SqlSessionFactory sqlSessionFactory;

   static {
       try {
           String resource = "mybatis-config.xml";
           InputStream inputStream = Resources.getResourceAsStream(resource);
           sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
      } catch (IOException e) {
           e.printStackTrace();
      }
  }

   //获取SqlSession连接
   public static SqlSession getSession(){
       return sqlSessionFactory.openSession();
  }

}
```

#### 2.5 创建实体类

```java
package com.study.entity;

import java.io.Serializable;

public class User implements Serializable {
    private Integer id;
    private String username;
    private String password;

    public User() {
    }

    public User(Integer id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}

```

#### 2.6 编写mapper接口

```java
package com.study.dao;

import com.study.entity.User;
import java.util.List;
import java.util.Map;

/**
 * @author huang
 */
//@Mapper
public interface UserMapper {
    //@Select("select * from t_user")
    List<User> getUserList(); 
    //模糊查询
    List<User> getUserLike(String value);
}

```

#### 2.7 编写Mapper.xml配置文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper   PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!--namespace 绑定一个对应的Dao/Mapper接口-->
<mapper namespace="com.study.dao.UserMapper">
    <!--select 查询语句-->
    <select id="getUserList" resultType="com.study.entity.User">
        select * from t_user
    </select>
</mapper>

```

#### 2.8 编写测试类

```java
@Test
public static void test(){
        SqlSession sqlSession = null;
        try {
            //1.获取SqlSession对象
            sqlSession = MybatisUtils.getSqlSession();
            //2.获取mapper对象
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            List<User> userList = mapper.getUserList();
            //不推荐使用
            //List<User> userList2 = sqlSession.selectList("com.study.dao.UserMapper.getUserList");
            for (User user : userList) {
                System.out.println(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //3.关闭资源sqlSession
            sqlSession.close();
        }
    }
```

#### 2.9 问题说明

**可能出现问题说明：Maven静态资源过滤问题**（xml配置文件不在resoures目录下的问题，会使xml文件访问不到）

在pom.xml文件加上(内容包裹在build标签下)

```xml
<build>
    <resources>
   <resource>
       <directory>src/main/java</directory>
       <includes>
           <include>**/*.properties</include>
           <include>**/*.xml</include>
       </includes>
       <filtering>false</filtering>
   </resource>
   <resource>
       <directory>src/main/resources</directory>
       <includes>
           <include>**/*.properties</include>
           <include>**/*.xml</include>
       </includes>
       <filtering>false</filtering>
   </resource>
</resources>
</build>
```



## 3、CRUD（增删改查）

#### 3.1 namespace

namespace中的包名要和mapper接口中的报名一致！

#### 3.2 select 

选择、查询语句

- id: 就是对应 namespace中的方法名；

- resultType ：sql 语句执行的返回值

- parameterType ： 参数类型

  

1.编写接口

```java
//根据id查询用户
User getUserById(Integer id);
//添加用户
int addUser(User user);
//更改用户信息
int updateUser(User user);
//删除用户
int deleteUser(Integer integer);
```

2.编写mapper中的SQL语句

```xml
<select id="getUserById" resultType="com.study.entity.User" parameterType="integer">
    select * from t_user where id= #{id}
</select>
```

3.测试

```java
public static void testSelect(){
    SqlSession sqlSession = MybatisUtils.getSqlSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    User user = mapper.getUserById(1);
    System.out.println(user);
    sqlSession.close();
}
```

#### 3.3 insert

```xml
<insert id="addUser" parameterType="com.study.entity.User" >
    insert into t_user (id,username,password) values (#{id},#{username},#{password});
</insert>
```

#### 3.4 update

```xml
<update id="updateUser" parameterType="com.study.entity.User">
    update t_user set username=#{username},password=#{password}  where id=#{id};
</update>
```

#### 3.5 delete

```xml
<delete id="deleteUser" parameterType="integer">
	delete from t_user where id = #{id}
</delete>
```

注意点：

- 增删改需要提交事务！

#### 3.6 分析错误

- 标签不要匹配错误

- resource 绑定 mapper的时候需要使用路径 /而不是.

- ```xml
  <mappers>
      <mapper resource="com/study/dao/UserMapper.xml"/>
  </mappers>
  ```

- 程序配置文件必须符合规范

- maven资源没有导出的问题

#### 3.7 万能的Map

假设我们的实体类，或者是数据库中的表，字段过多，我们应当考虑使用Map！

```java
//使用map来控制参数
int addUser2(Map map);
```

```xml
<insert id="addUser2" parameterType="map">
    insert into t_user (id,username,password) values (#{id},#{name},#{pwd});
</insert>
```

```java
public static void addUser2(){
    SqlSession sqlSession = MybatisUtils.getSqlSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    Map<String, Object> map = new HashMap<>();
    map.put("id",7);
    map.put("name","clearLove7");
    map.put("pwd","7777777");
    int res = mapper.addUser2(map);
    if (res > 0) {
        System.out.println("增加成功");
        sqlSession.commit();
        System.out.println("事务已经提交");
    } else {
        System.out.println("增加失败，事务回滚");
        sqlSession.rollback();
    }
    sqlSession.close();
}
```

Map传递参数，直接在sql中取出key即可！   【parameterType="map"】

对象传递参数，直接在sql中取对象的属性即可！【parameterType="com.study.entity.User"】

只有一个基本数据类型参数的情况下，可以直接在sql取到！ 可以不写

多个参数用Map，或者**注解**

#### 3.8 模糊查询

- 在java代码执行的时候，传递通配符% %

- ```java
  List<User> userList = mapper.getUserLike("%李%");
  ```

- ```xml
  <select id="getUserLike" resultType="com.study.entity.User">
      select * from t_user where username like #{value}
  </select>
  ```

- 在sql拼接中使用通配符

- ```java
  List<User> userList = mapper.getUserLike("李");
  ```

- ```xml
  <select id="getUserLike" resultType="com.study.entity.User">
      select * from t_user where username like "%"#{value}"%"
  </select>
  ```



## 4、配置解析

#### 4.1 核心配置文件

mybatis-config.xml

```xml
• configuration（配置） 
• properties（属性） 
• settings（设置） 
•typeAliases（类型别名） 
• typeHandlers（类型处理器） 
• objectFactory（对象工厂） 
• plugins（插件） 
• environments（环境配置） 
    ▪ environment（环境变量） 
    ▪ transactionManager（事务管理器） 
    ▪ dataSource（数据源） 
• databaseIdProvider（数据库厂商标识） 
• mappers（映射器） 
```

#### 4.2 **环境配置（environments）**

Mybatis可以配置成适应多种环境

**不过要记住：尽管可以配置多种环境，但每个SqlSessionFactory实例只能选择一种环境**

学会使用配置多套运行环境！

MyBatis默认的事务管理器就是JDBC ,连接池：POOLED

#### 4.3 **属性（properties）**

我们可以通过properties属性来实现引用配置文件

这些属性可以在外部进行配置，并可以进行动态替换。你既可以在典型的 Java 属性文件中配置这些属性，也可以在 properties 元素的子元素中设置。【db.properties】【log4j.properties】

![image-20220412233939838](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412233939838.png)

编写一个配置文件db.properties

```properties
driver=com.mysql.jdbc.Driver
url=jdbc:mysql://localhost:3306/mybatis
username=root
password=root
```

在核心配置文件中引入

```xml
<!--引入外部配置文件-->
<properties resource="db.properties"/>
```

注意点：

- 可以直接引入外部配置文件
- 可以在其中增加一些属性配置
- 如果两个文件有相同字段，优先使用外部配置文件的！

#### 4.4 **类型别名（typeAliases）**

类型别名可为 Java 类型设置一个缩写名字。 

它仅用于 XML 配置，意在降低冗余的全限定类名书写 比如：

```xml
<!--给实体类起别名-->
<typeAliases>
    <typeAlias type="com.study.entity.User" alias="User"/>
</typeAliases>
```

也可以指定一个包名，MyBatis 会在包名下面搜索需要的 Java Bean 比如：

扫描实体类的包，它的默认别名就是这个类的 类名，首字母小写！

```xml
<!--扫描指定的包-->
<typeAliases>
    <package name="com.study.entity"/>
</typeAliases>
```

在实体类比较少的时候，使用第一种方式。

如果实体类比较多，建议使用第二种。

第一种别名可以DIY，第二种不行，如果非要改，需要在实体类中写上注解

```java
@Alias("user")
public class User{}
```

#### 4.5 **设置（settings）**

- 缓存开启关闭
- 懒加载
- 日志实现

![image-20220412234007388](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234007388.png)

![image-20220412234027248](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234027248.png)

#### 4.6 映射器（mappers）

MapperRegistry：注册绑定我们的Mapper文件；

方式一：【推荐使用】

```xml
<!-- 使用相对于类路径的资源引用 -->
<mappers>
    <mapper resource="com/study/dao/UserMapper.xml"/>
</mappers>
```

方式二：

````xml
<!-- 使用映射器接口实现类的完全限定类名 -->
<mappers>
    <mapper class="com.study.dao.UserMapper"/>
</mappers>
````

注意点：

- 接口和它的Mapper配置文件必须在同一个包下！
- 接口和它的Mapper配置文件必须同名！

方式三：

```xml
<!-- 将包内的映射器接口实现全部注册为映射器 -->
<mappers>
    <package name="com.study.dao"/>
</mappers>
```

注意点：

- 接口和它的Mapper配置文件必须在同一个包下！
- 接口和它的Mapper配置文件必须同名！

常见错误：

**org.apache.ibatis.binding.BindingException: Type interface com.study.dao.UserMapper is not known to the MapperRegistry.**

**说明你的mapper配置文件没有在核心配置文件中绑定**



#### 4.7**作用域（Scope）和生命周期**

**作用域**和**生命周期**是至关重要的，因为错误的使用会导致非常严重的**并发问题**。

![image-20220412234044432](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234044432.png)

**SqlSessionFactoryBuilder**

- 一旦创建了 SqlSessionFactory，就不再需要它了
- 局部变量

**SqlSessionFactory**

- 说白了就是可以想象成： 数据库连接池
- SqlSessionFactory 一旦被创建就应该在应用的运行期间一直存在，**没有任何理由丢弃它或重新创建另一个实例**
- 因此SqlSessionFactory 的最佳作用域是应用作用域
- 最简单的就是使用**单例模式**或者**静态单例模式**

**SqlSession**

- SqlSession的实例不是线程安全的，因此是不能被共享的，所以它的最佳的作用域是请求或方法作用域
- 连接到连接池的一个请求！
- 用完之后需要赶紧关闭，否则资源被占用！

![image-20220412234101440](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234101440.png)

这里的每一个mapper都代表一个业务！



## 5、ResultMap（解决属性名和字段名不一致的问题）

#### 5.1 测试问题：

1、 数据库的字段名

![image-20220412234119168](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234119168.png)

2、Java中的实体类

```java
public class User {

   private int id;  //id
   private String name;   //姓名
   private String password;   //密码和数据库不一样！
   
   //构造
   //set/get
   //toString()
}
```

3、接口

```java
//根据id查询用户
User selectUserById(int id);
```

4、mapper映射文件

```xml
<select id="selectUserById" resultType="user">
  select * from user where id = #{id}
</select>
```

5、测试

```java
@Test
public void testSelectUserById() {
   SqlSession session = MybatisUtils.getSession();  //获取SqlSession连接
   UserMapper mapper = session.getMapper(UserMapper.class);
   User user = mapper.selectUserById(1);
   System.out.println(user);
   session.close();
}
```

**结果：**

- User{id=1, name='hhwww', password='null'}
- 查询出来发现 password 为空 . 说明出现了问题！

**分析：**

- select * from user where id = #{id} 可以看做  select  **id,name,pwd**  from user where id = #{id}
- mybatis会根据这些查询的列名(会将列名转化为小写,数据库不区分大小写) , 去对应的实体类中查找相应列名的set方法设值 , 由于找不到setPwd() , 所以password返回null ; 【**自动映射**】

#### 5.2 解决问题

方案一：为列名指定别名 , 别名和java实体类的属性名一致

```xml
<select id="selectUserById" resultType="User">
  select id , name , pwd as password from user where id = #{id}
</select>
```

方案二：**使用结果集映射->ResultMap** 【推荐】

```xml
<!-- id就是resultMap的名字 type是要映射的类型 -->
<resultMap id="UserMap" type="User">
   <!-- id为主键 -->
   <id column="id" property="id"/>
   <!-- column是数据库表的列名 , property是对应实体类的属性名 -->
   <result column="name" property="name"/>
   <result column="pwd" property="password"/>
</resultMap>

<select id="selectUserById" resultMap="UserMap">
  select id , name , pwd from user where id = #{id}
</select>
```

#### 5.3 resultMap

结果集映射

```
id	name	pwd   (数据的)
id	name	password   (实体类的)
```



- resultMap 元素是 MyBatis 中最重要最强大的元素。它可以让你从 90% 的 JDBC `ResultSets` 数据提取代码中解放出来。

- resultMap 的设计思想是，对于简单的语句根本不需要配置显式的结果映射，而对于复杂一点的语句只需要描述它们的关系就行了
- resultMap 最优秀的地方在于，虽然你已经对它相当了解了，但是根本就不需要显式地用到他们。(什么不一样，才需要映射什么)



## 6、日志

#### 6.1 日志工厂

如果一个数据库操作，出现了异常，我们需要排错。日志就是最好的助手！

曾经：sout打印、debug跑日志

现在：日志工厂！

![image-20220412234138009](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234138009.png)

- LOG4J【掌握】
- STDOUT_LOGGING   【掌握】

其他的需要了解即可

在Mybatis中具体需要使用哪个日志实现，在设置中设定！

```xml
<settings>
    <setting name="logImpl" value="STDOUT_LOGGING"/>
</settings>
```

**STDOUT_LOGGING**   标准日志输出

![image-20220412234156100](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234156100.png)

#### 6.2 Log4j

什么是Log4j？

- Log4j是Apache的一个开源项目，通过使用Log4j，我们可以控制日志信息输送的目的地：控制台，文本，GUI组件....
- 我们也可以控制每一条日志的输出格式；
- 通过定义每一条日志信息的级别，我们能够更加细致地控制日志的生成过程。最令人感兴趣的就是，这些可以通过一个配置文件来灵活地进行配置，而不需要修改应用的代码。

1、先导入Log4j的包

```xml
<dependency>
   <groupId>log4j</groupId>
   <artifactId>log4j</artifactId>
   <version>1.2.17</version>
</dependency>
```

2、log4j.properties

```properties
#将等级为DEBUG的日志信息输出到console和file这两个目的地，console和file的定义在下面的代码
log4j.rootLogger=DEBUG,console,file

#控制台输出的相关设置
log4j.appender.console = org.apache.log4j.ConsoleAppender
log4j.appender.console.Target = System.out
log4j.appender.console.Threshold=DEBUG
log4j.appender.console.layout = org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=[%c]-%m%n

#文件输出的相关设置
log4j.appender.file = org.apache.log4j.RollingFileAppender
log4j.appender.file.File=./log/kuang.log
log4j.appender.file.MaxFileSize=10mb
log4j.appender.file.Threshold=DEBUG
log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=[%p][%d{yy-MM-dd}][%c]%m%n

#日志输出级别
log4j.logger.org.mybatis=DEBUG
log4j.logger.java.sql=DEBUG
log4j.logger.java.sql.Statement=DEBUG
log4j.logger.java.sql.ResultSet=DEBUG
log4j.logger.java.sql.PreparedStatement=DEBUG
```

3、配置log4j为日志的实现

```xml
<settings>
        <!--Log4j实现-->
        <setting name="logImpl" value="LOG4J"/>
    </settings>
```

4、Log4j日志输出

![image-20220412234214170](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234214170.png)

5、**简单使用log4j**

1、在要使用Log4j的类中，导入包 	import org.apache.log4j.Logger;

2、日志对象，参数为当前类的反射(class)

```java
static Logger logger = Logger.getLogger(TestUserMapper.class);
```

3、日志级别

```java
logger.info("info：进入selectUser方法");
logger.debug("debug：进入selectUser方法");
logger.error("error: 进入selectUser方法");
```



## 7、分页

​	思考：为什么分页？

- 核心 ：减少数据的处理量
- 在学习mybatis等持久层框架的时候，会经常对数据进行增删改查操作，使用最多的是对数据库进行查询操作，如果查询大量数据的时候，我们往往使用分页进行查询，也就是每次处理小部分数据，**这样对数据库压力就在可控范围内**。

#### **7.1 SQL底层使用limit进行分页**

```sql
#语法
SELECT * FROM table LIMIT stratIndex，pageSize

SELECT * FROM table LIMIT 5,10; // 检索记录行 6-15  

SELECT * FROM table LIMIT 5; [0,5]
```

#### **7.2使用Mybatis分页，核心SQL**

万能Map实现步骤：

1、写接口

```java
List<User> getUserByLimit(HashMap<String,Integer> map);
```

2、mapper.xml

```xml
<select id="getUserByLimit" resultMap="userMap" parameterType="map">
    select * from t_user limit #{startIndex},#{pageSize}
</select>
```

3、测试

```java
@Test
    public void testLimit(){
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("startIndex",0);
        map.put("pageSize",2);

        List<User> userList = mapper.getUserByLimit(map);
        for (User user : userList) {
            System.out.println(user);
        }
        sqlSession.close();
    }
```



#### 7.3 使用RowBounds进行分页

不在使用SQL进行分页

1、接口

```java
/**
 * 使用RowBounds进行分页
 * @return
 */
List<User> getUserbyRowBounds();
```

2、mapper.xml

```xml
<select id="getUserbyRowBounds" resultMap="userMap">
    select * from t_user
</select>
```

3、测试

```java
@Test
public void testRowBounds(){
    SqlSession sqlSession = MybatisUtils.getSqlSession();

    //RowBounds实现
    RowBounds rowBounds = new RowBounds(1,2);

    //通过java代码层面实现分页
    List<User> userList = 	           			   sqlSession.selectList("com.study.dao.UserMapper.getUserbyRowBounds",null,rowBounds);

    for (User user : userList) {
        System.out.println(user);
    }
    sqlSession.close();
}
```



#### 7.4 Mybatis分页插件

![image-20220412234231439](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234231439.png)

了解即可，可以自己尝试使用

官方文档：https://pagehelper.github.io/



## 8、使用注解开发

#### 8.1 面向接口编程

- 大家之前都学过面向对象编程，也学习过接口，但在真正的开发中，很多时候我们会选择面向接口编程

- **根本原因 : ==解耦== , 可拓展 , 提高复用 , 分层开发中 , 上层不用管具体的实现 , 大家都遵守共同的标准 , 使得开发变得容易 , 规范性更好**

- 在一个面向对象的系统中，系统的各种功能是由许许多多的不同对象协作完成的。在这种情况下，各个对象内部是如何实现自己的,对系统设计人员来讲就不那么重要了；

- 而各个对象之间的协作关系则成为系统设计的关键。小到不同类之间的通信，大到各模块之间的交互，在系统设计之初都是要着重考虑的，这也是系统设计的主要工作内容。面向接口编程就是指按照这种思想来编程。

  

**关于接口的理解**

- 接口从更深层次的理解，应是定义（规范，约束）与实现（名实分离的原则）的分离。

- 接口的本身反映了系统设计人员对系统的抽象理解。

- 接口应有两类：

- - 第一类是对一个个体的抽象，它可对应为一个抽象体(abstract class)；
  - 第二类是对一个个体某一方面的抽象，即形成一个抽象面（interface）；

- 一个体有可能有多个抽象面。抽象体与抽象面是有区别的。



**三个面向区别**

- 面向对象是指，我们考虑问题时，以对象为单位，考虑它的属性及方法 .
- 面向过程是指，我们考虑问题时，以一个具体的流程（事务过程）为单位，考虑它的实现 .
- 接口设计与非接口设计是针对复用技术而言的，与面向对象（过程）不是一个问题.更多的体现就是对系统整体的架构



#### 8.2 使用注解开发

1、注解在接口上实现

```java
@Select("select * from t_user")
List<User> getUsers();
```

2、需要在核心配置文件中绑定接口！

```xml
<mappers>
    <mapper class="com.study.dao.UserMapper"/>
</mappers>
```

3、测试

4、利用dubug查看本质：反射机制实现

![image-20220412234300527](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234300527.png)

5、底层：本质上利用了jvm的动态代理机制！

![image-20220412234411674](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234411674.png)



#### 8.3 Mybatis详细执行流程

![image-20220412234437474](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234437474.png)



## 9、注解CRUD

#### 9.1 步骤：

1、改造MybatisUtils工具类的getSession()方法，重载实现  

作用：自动提交事务

```java
//获取SqlSession连接
public static SqlSession getSession(){
    return getSession(true); //事务自动提交
}

public static SqlSession getSession(boolean flag){
    return sqlSessionFactory.openSession(flag);
}
```

2、编写接口方法和注解

```java
public interface UserMapper {
    @Select("select * from t_user")
    List<User> getUsers();

    @Insert("insert into t_user values (#{id},#{username},#{password})")
    int addUser(User user);

    @Update("update t_user set username=#{username},password=#{password} where id=#{id}")
    int updateUser(User user);
	
    //如果方法存在多个参数，所有参数前面必须加上@param 注解 （只基本类型的需要加吧）
    @Delete("delete from t_user where id=#{uid}")
    int deleteUser(@Param("uid") int id);
}

```

3、测试

```java
public class TestUserMapper {
    @Test
    public void getUsers(){
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        //底层主要应用反射
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        List<User> users = mapper.getUsers();
        for (User user : users) {
            System.out.println(user);
        }
    }

    @Test
    public void addUser(){
        SqlSession sqlSession = MybatisUtils.getSqlSession();

        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        mapper.addUser(new User(8,"hw","123456"));
    }

    @Test
    public void updateUser(){
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        mapper.updateUser(new User(2,"zzf","0601"));
    }

    @Test
    public void deleteUser(){
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        mapper.deleteUser(5);
    }

}
```



#### 9.2 关于@Param()注解

- 基本类型的参数和String类型，需要加上
- 引用类型不需要加上
- 如果只有一个基本类型的话，可以忽略，但是建议大家都加上！
- 我们在SQL中引用的就是我们在这里@Param("")中设定的属性名！



**#{} 和${}**

#{} 相当 预编译的preperedstatment  可以有效的防止sql注入

${}  就是普通的statment 会造成外界可以拼接SQL，从而导致SQL注入，不安全



## 10、Lombok

#### 10.1 使用步骤

1、在IDEA中安装Lombok插件!

2、在项目中导入lombok的 jar包

```xml
<!-- https://mvnrepository.com/artifact/org.projectlombok/lombok -->
<dependency>
 <groupId>org.projectlombok</groupId>
 <artifactId>lombok</artifactId>
 <version>1.16.10</version>
</dependency>
```

3、在实体类上加注解即可

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    private Integer id;
    private String username;
    private String password;

}
```



@Data ： 无参构造，get，set，tostring，hashcode，equals

@AllArgsConstructor : 全部参数的构造器
@NoArgsConstructor：无参构造器



## 10、多对一处理

#### 10.1数据库设计

```sql
CREATE TABLE `teacher` (
`id` INT(10) NOT NULL,
`name` VARCHAR(30) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8

INSERT INTO teacher(`id`, `name`) VALUES (1, '秦老师');

CREATE TABLE `student` (
`id` INT(10) NOT NULL,
`name` VARCHAR(30) DEFAULT NULL,
`tid` INT(10) DEFAULT NULL,
PRIMARY KEY (`id`),
KEY `fktid` (`tid`),
CONSTRAINT `fktid` FOREIGN KEY (`tid`) REFERENCES `teacher` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8


INSERT INTO `student` (`id`, `name`, `tid`) VALUES ('1', '小明', '1');
INSERT INTO `student` (`id`, `name`, `tid`) VALUES ('2', '小红', '1');
INSERT INTO `student` (`id`, `name`, `tid`) VALUES ('3', '小张', '1');
INSERT INTO `student` (`id`, `name`, `tid`) VALUES ('4', '小李', '1');
INSERT INTO `student` (`id`, `name`, `tid`) VALUES ('5', '小王', '1');
```

#### 10.2 测试运行环境

1、导入lombok

2、新建实体类

3、建立Mapper接口

4、建立Mapper.xml文件

5、在核心配置文件中绑定注册我们的Mapper接口或者文件

6、测试



#### 10.3 多对一按照查询嵌套处理

实体类

```java
/**
 * 多个学生对应一个老师
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private int id;
    private String name;
    private Teacher teacher;
}
```

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Teacher {
    int id;
    private String name;

}
```

```xml
<!--
  思路:1.查询所有的学生信息
       2.根据查询出来的学生的tid，寻找对应的老师！
    -->
<select id="getStudentList" resultMap="StudentTeacher">
    select * from student
</select>
<resultMap id="StudentTeacher" type="Student">
    <result property="id" column="id"/>
    <result property="name" column="name"/>
    <!--复杂的属性需要单独处理  association:对象    collection：集合-->
    <association property="teacher" column="tid" javaType="Teacher" select="getTeacher"/>
</resultMap>
<select id="getTeacher" resultType="Teacher">
    select * from teacher where id = #{id};
</select>
```



#### 10.4 按照结果嵌套处理

起别名，不改变sql，进行查询 (一步到位) 主要是配置结果集的映射

```xml
<!--按照结果嵌套查询-->
<select id="getStudentList2" resultMap="StudentTeacher2">
    select s.id as sid,s.name as sname,t.name as tname
    from student s,teacher t
    where s.tid = t.id
</select>
<resultMap id="StudentTeacher2" type="Student">
    <result property="id" column="sid"/>
    <result property="name" column="sname"/>
    <association property="teacher" javaType="Teacher">
        <result property="name" column="tname"/>
    </association>
</resultMap>
```



#### 10.5 回顾Mysql多对一查询方式

- 子查询
- 联表查询



## 11、一对多处理

比如：一个老师拥有多个学生！

对于老师而言，就是一对多的关系。

实体类

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Teacher {
    int id;
    private String name;
    /**
     * 一个老师对应多个学生
     */
    private List<Student> students;

}
```

```java
/**
 *  一个老师对应多个学生
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private int id;
    private String name;
    private int tid;
}

```

#### 11.1 按照结果进行嵌套查询

```xml
<select id="getTeacherStudent" resultMap="TeacherStudent">
    select s.id as sid, s.name as sname,t.name as tname,t.id as tid
    from teacher t ,student s
    where (t.id=#{tid}) and (t.id=s.tid);
</select>
<resultMap id="TeacherStudent" type="Teacher">
    <result property="id" column="tid"/>
    <result property="name" column="tname"/>
    <!--
       复杂的属性需要单独处理  association:对象   collection：集合
       javaType：指定属性的类型
       集合中的泛型信息，我们使用ofType获取
       -->
    <collection property="students" ofType="Student">
        <result property="id" column="sid"/>
        <result property="name" column="sname"/>
        <result property="tid" column="tid"/>
    </collection>
</resultMap>
```

接口

```java
/**
 * 获取指定老师下面的所有学生的信息
 */
Teacher getTeacherStudent(@Param("tid") int id);
```



#### 10.2 总结：

1、关联：association  【多对一】

2、集合：collection     【一对多】

3、javaType  & ofType

​	javaType  ：用来指定实体类中属性的类型

​	ofType：用来指定映射到List或者集合中的pojo类型，泛型中的约束类型！



注意点：

- 保证sql的可读性，尽量保证通俗易懂
- 注意一对多和多对一的 属性名和字段名的问题
- 如果问题不好排查错误，可以使用日志，建议使用 [log4j]

面视高频:

- Mysql 引擎  InnoDB 和 MyISAM
- InnoDB 底层原理
- 索引
- 索引优化！



## 12、动态SQL

什么是动态SQL：**动态SQL指的是根据不同的查询条件 , 生成不同的Sql语句.**

```xml
动态 SQL 元素和 JSTL 或基于类似 XML 的文本处理器相似。在 MyBatis 之前的版本中，有很多元素需要花时间了解。MyBatis 3 大大精简了元素种类，现在只需学习原来一半的元素便可。MyBatis 采用功能强大的基于 OGNL 的表达式来淘汰其它大部分元素。

  -------------------------------
  - if
  - choose (when, otherwise)
  - trim (where, set)
  - foreach
  -------------------------------
```

#### 12.1 搭建环境

1、数据库

```sql
CREATE TABLE `blog` (
`id` varchar(50) NOT NULL COMMENT '博客id',
`title` varchar(100) NOT NULL COMMENT '博客标题',
`author` varchar(30) NOT NULL COMMENT '博客作者',
`create_time` datetime NOT NULL COMMENT '创建时间',
`views` int(30) NOT NULL COMMENT '浏览量'
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

2、实体类

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blog {
    private int id;
    private String title;
    private String author;
    private Date create_time;
    private int views;
}
```

3、编写接口

4、接口的mappe.xml文件

5、插入数据



#### 12.2 IF(where)

```xml
<select id="queryBlogIF" parameterType="map" resultType="Blog">
    select * from blog
    <where>
        <if test="title != null">
            title = #{title}
        </if>
        <if test="author != null">
            and author = #{author}
        </if>
    </where>
</select>
```

- 这个“where”标签会知道如果它包含的标签中有返回值的话，它就插入一个‘where’。此外，如果标签返回的内容是以AND 或OR 开头的，则它会剔除掉。



#### 12.3 Choose

```xml
<select id="queryBlogChoose" parameterType="map" resultType="Blog">
    select * from blog
    <where>
        <choose>
            <when test="title != null">
                title = #{title}
            </when>
            <when test="author != null">
                and author = #{author}
            </when>
            <otherwise>
                and views = #{views}
            </otherwise>
        </choose>
    </where>
</select>
```

#### 12.4 trim（where，set）

```xml
<update id="updateBlog" parameterType="map">
    update blog
    <set>
        <if test="title != null">
            title = #{title},
        </if>
        <if test="author != null">
            author = #{author}
        </if>
    </set>
    where id = #{id}
</update>
```



#### 12.5 SQL片段

有时候可能某个 sql 语句我们用的特别多，为了增加代码的重用性，简化代码，我们需要将这些代码抽取出来，然后使用时直接调用。

```xml
<sql id="if-title-author">
    <if test="title != null">
        title = #{title}
    </if>
    <if test="author != null">
        and author = #{author}
    </if>
</sql>
<!-- 引用 sql 片段，如果refid 指定的不在本文件中，那么需要在前面加上 namespace -->
<select id="queryBlogIF" parameterType="map" resultType="Blog">
    select * from blog
    <where>
        <include refid="if-title-author"/>
        <!-- 在这里还可以引用其他的 sql 片段 -->
    </where>
</select>
```

**引用 sql 片段，如果refid 指定的不在本文件中，那么需要在前面加上 namespace** 

注意：

①、最好基于 单表来定义 sql 片段，提高片段的可重用性

②、在 sql 片段中不要包括 where

#### 12.6 Foreach

```xml
<!--select * from blog where 1=1 and (id=1 or id=2 or id=3)-->
<!-- collection:要遍历的集合 
	 item：每次遍历生成的对象
	 open:开始遍历时的拼接字符串
     close:结束时拼接的字符串
     separator:遍历对象之间需要拼接的字符串
-->
<select id="queryBlogForeach" resultType="Blog" parameterType="map">
    select * from blog
    <where>
        <foreach collection="ids" item="id" open="and (" separator="or" close=")">
            id = #{id}
        </foreach>
    </where>
</select>
```

测试

```java
@Test
public void selectForeach(){
    SqlSession sqlSession = MybatisUtils.getSqlSession();
    BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);
    Map<String, Object> map = new HashMap<String, Object>();

    ArrayList<Integer> ids = new ArrayList<Integer>();
    ids.add(1);
    ids.add(2);
    map.put("ids",ids);
    List<Blog> blogs = mapper.queryBlogForeach(map);
    for (Blog blog : blogs) {
        System.out.println(blog);
    }

    sqlSession.close();
}
```

**所谓的动态SQL，本质上还是SQL，只是我们在SQL层面，去执行了一个逻辑代码**

==动态SQL就是在拼接SQL语句，我们只要保证SQL的正确性，按照SQL的格式，去排列组合就可以了==

建议

- 先在Mysql中写出完整的SQL，再去对应的去修改成为我们的动态SQL实现通用即可！
- 动态SQL在开发中大量的使用，一定要熟练掌握！



## 13、缓存

#### 13.1 简介

1、什么是缓存 [ Cache ]？

- 存在内存中的临时数据。
- 将用户经常查询的数据放在缓存（内存）中，用户去查询数据就不用从磁盘上(关系型数据库数据文件)查询，从缓存中查询，从而提高查询效率，解决了高并发系统的性能问题。

2、为什么使用缓存？

- 减少和数据库的交互次数，减少系统开销，提高系统效率。

3、什么样的数据能使用缓存？

- 经常查询并且不经常改变的数据。



#### 13.2 Mybatis缓存

- MyBatis包含一个非常强大的查询缓存特性，它可以非常方便地定制和配置缓存。缓存可以极大的提升查询效率。

- MyBatis系统中默认定义了两级缓存：**一级缓存**和**二级缓存**

- - 默认情况下，只有一级缓存开启。（SqlSession级别的缓存，也称为本地缓存）
  - 二级缓存需要手动开启和配置，他是基于namespace级别的缓存。（mapper缓存）
  - 为了提高扩展性，MyBatis定义了缓存接口Cache。我们可以通过实现Cache接口来自定义二级缓存



#### 13.3 一级缓存

一级缓存也叫本地缓存：

- 与数据库同一次会话期间查询到的数据会放在本地缓存中。
- 以后如果需要获取相同的数据，直接从缓存中拿，没必须再去查询数据库；

测试:

1、开启日志

2、编写接口方法和对应的Mapper.xml

```java
public interface UserMapper {
    User queryUser(@Param("id") int id);
}
```

```xml
<select id="queryUser" resultType="User">
    select * from t_user where id = #{id};
</select>
```

3、测试

```java
@Test
public void testQueryUser(){
    SqlSession sqlSession = MybatisUtils.getSqlSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    User user = mapper.queryUser(2);
    System.out.println(user);
    System.out.println("===========================");
    User user2 = mapper.queryUser(2);
    System.out.println(user2);
    sqlSession.close();
}
```

![image-20220412234501746](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234501746.png)



#### 13.4 一级缓存失效的四种情况

一级缓存是SqlSession级别的缓存，是一直开启的，我们关闭不了它；

一级缓存失效情况：没有使用到当前的一级缓存，效果就是，还需要再向数据库中发起一次查询请求！

**1、sqlSession不同****

```java
@Test
public void testQueryUserById(){
   SqlSession session = MybatisUtils.getSession();
   SqlSession session2 = MybatisUtils.getSession();
   UserMapper mapper = session.getMapper(UserMapper.class);
   UserMapper mapper2 = session2.getMapper(UserMapper.class);

   User user = mapper.queryUserById(1);
   System.out.println(user);
   User user2 = mapper2.queryUserById(1);
   System.out.println(user2);
   System.out.println(user==user2);

   session.close();
   session2.close();
}
```

观察结果：发现发送了两条SQL语句！

结论：**每个sqlSession中的缓存相互独立**

**2、sqlSession相同，查询条件不同**

```java
@Test
public void testQueryUserById(){
   SqlSession session = MybatisUtils.getSession();
   UserMapper mapper = session.getMapper(UserMapper.class);
   UserMapper mapper2 = session.getMapper(UserMapper.class);

   User user = mapper.queryUserById(1);
   System.out.println(user);
   User user2 = mapper2.queryUserById(2);
   System.out.println(user2);
   System.out.println(user==user2);

   session.close();
}
```

观察结果：发现发送了两条SQL语句！很正常的理解

结论：**当前缓存中，不存在这个数据**

**3、sqlSession相同，两次查询之间执行了增删改操作**

```java
@Test
public void testQueryUserById(){
   SqlSession session = MybatisUtils.getSession();
   UserMapper mapper = session.getMapper(UserMapper.class);

   User user = mapper.queryUserById(1);
   System.out.println(user);

   HashMap map = new HashMap();
   map.put("name","kuangshen");
   map.put("id",4);
   mapper.updateUser(map);

   User user2 = mapper.queryUserById(1);
   System.out.println(user2);

   System.out.println(user==user2);

   session.close();
}
```

观察结果：查询在中间执行了增删改操作后，重新执行了

结论：**因为增删改操作可能会对当前数据产生影响**

**4、sqlSession相同，手动清除一级缓存**

```java
@Test
public void testQueryUserById(){
   SqlSession session = MybatisUtils.getSession();
   UserMapper mapper = session.getMapper(UserMapper.class);

   User user = mapper.queryUserById(1);
   System.out.println(user);

   session.clearCache();//手动清除缓存

   User user2 = mapper.queryUserById(1);
   System.out.println(user2);

   System.out.println(user==user2);

   session.close();
}
```

一级缓存就是一个map



#### 13.5 二级缓存

- 只有一级缓存不起作用的时候，才使用二级缓存

- 二级缓存也叫全局缓存，一级缓存作用域太低了，所以诞生了二级缓存

- 基于namespace级别的缓存，一个名称空间，对应一个二级缓存；

- 工作机制

- - 一个会话查询一条数据，这个数据就会被放在当前会话的一级缓存中；
  - 如果当前会话关闭了，这个会话对应的一级缓存就没了；但是我们想要的是，会话关闭了，一级缓存中的数据被保存到二级缓存中；
  - 新的会话查询信息，就可以从二级缓存中获取内容；
  - 不同的mapper查出的数据会放在自己对应的缓存（map）中；

使用步骤：

1、开启全局缓存 【mybatis-config.xml】

```xml
<setting name="cacheEnabled" value="true"/>
```

2、去每个mapper.xml中配置使用二级缓存

```xml
<cache
 eviction="FIFO"
 flushInterval="60000"
 size="512"
 readOnly="true"/>
这个更高级的配置创建了一个 FIFO 缓存，每隔 60 秒刷新，最多可以存储结果对象或列表的 512 个引用，而且返回的对象被认为是只读的，因此对它们进行修改可能会在不同线程中的调用者产生冲突。
```

3、测试

- 所有的实体类先实现序列化接口
- 测试代码

```java
@Test
public void testQueryUserById(){
   SqlSession session = MybatisUtils.getSession();
   SqlSession session2 = MybatisUtils.getSession();

   UserMapper mapper = session.getMapper(UserMapper.class);
   UserMapper mapper2 = session2.getMapper(UserMapper.class);

   User user = mapper.queryUserById(1);
   System.out.println(user);
   session.close();

   User user2 = mapper2.queryUserById(1);
   System.out.println(user2);
   System.out.println(user==user2);

   session2.close();
}
```

结论：

- 只要开启了二级缓存，我们在同一个Mapper中的查询，可以在二级缓存中拿到数据

- 查出的数据都会被默认先放在一级缓存中

- 只有会话提交或者关闭以后，一级缓存中的数据才会转到二级缓存中

  

#### 13.6 缓存原理

![image-20220412234523329](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234523329.png)

![image-20220412234538673](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412234538673.png)

注意：

1、先看二级缓存中有没有

2、再看一级缓存用有没有

3、最后查询数据库