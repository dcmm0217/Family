# 第1节-ShardingSphere快速开始 

## 1、ShardingSphere介绍  

#### 1.1 Apache ShardingSphere  

Apache ShardingSphere 是⼀套开源的分布式数据库解决⽅案组成的⽣态圈，它由 JDBC、Proxy 和 Sidecar（规划中）这 3 款既能够独⽴部署，⼜⽀持混合部署配合使⽤的产品组成。它们均提供标准化的数据⽔平扩展、分布式事务和分布式治理等功能，可适⽤于如 Java 同构、异构语⾔、云原⽣等各种多样化的应⽤场景。  

Apache ShardingSphere 旨在充分合理地在分布式的场景下`利⽤关系型数据库的计算和存储能⼒`，⽽并⾮实现⼀个全新的关系型数据库。关系型数据库当今依然占有巨⼤市场份额，是企业核⼼系统的基⽯，未来也难于撼动，我们更加注重在原有基础上提供增量，⽽⾮颠覆。  

ShardingSphere 已于2020年4⽉ 16⽇成为 Apache 软件基⾦会的顶级项⽬。  

#### 1.2 ShardingSphere-JDBC  

==定位为轻量级 Java 框架，在 Java 的 JDBC 层提供的额外服务。==  

==它使⽤客户端直连数据库，以 jar 包形式提供服务，⽆需额外部署和依赖，可理解为增强版的 JDBC 驱动，完全兼容JDBC 和各种 ORM 框架。==  

特点：

- 适用于任何基于JDBC的ORM框架，如：JPA、HIbernate、Mybatis、Spring JDBC Template或直接使用原生JDBC；
- 支持任何第三方数据库连接池，如：DBCP、C3P0、BoneCP、HikariCP等
- 支持任意实现JDBC规范的数据库，目前支持MySQL，PostgreSQL，Oracle，SQLServer等以及任何可使用JDBC访问的数据库。

![image-20220821174223270](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/imgimage-20220821174223270.png)

#### 1.3 ShardingSphere-Proxy  

==定位为透明化的数据库代理端==，提供封装了数据库⼆进制协议的服务端版本，⽤于完成对异构语⾔的⽀持。⽬前提供 MySQL 和 PostgreSQL（兼容 openGauss 等基于 PostgreSQL 的数据库）版本  ，它可以使⽤任何兼容 MySQL/PostgreSQL 协议的访问客户端（如： MySQLCommand Client, MySQL Workbench, Navicat 等）操作数据，对 DBA 更加友好。  

特点：

- 向应⽤程序完全透明，可直接当做 MySQL/PostgreSQL 使⽤；  
- 适⽤于任何兼容 MySQL/PostgreSQL 协议的的客户端。  

![image-20220821174556530](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/imgimage-20220821174556530.png)

#### 1.4 ShardingSphere-Sidecar（TODO）  

`定位为 Kubernetes 的云原⽣数据库代理`  ，以 Sidecar 的形式代理所有对数据库的访问。  ==通过⽆中⼼、零侵⼊的⽅案提供与数据库交互的啮合层，即 Database Mesh ，⼜可称数据库⽹格。==  

Database Mesh 的关注重点在于如何将分布式的数据访问应⽤与数据库有机串联起来，它更加关注的是交互，是将杂乱⽆章的应⽤与数据库之间的交互进⾏有效地梳理。使⽤Database Mesh，访问数据库的应⽤和数据库终将形成⼀个巨⼤的⽹格体系，应⽤和数据库只需在⽹格体系中对号⼊座即可，它们都是被啮合层所治理的对象。  

![image-20220821175318446](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/imgimage-20220821175318446.png)

==差距总结：==

![image-20220821175335713](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/imgimage-20220821175335713.png)

## 2、快速开始

#### 2.1 准备环境

使⽤docker部署第⼀台MySQL服务器，先来快速体验ShardingSphere带来的分表功能。在之后的分库分表中，再加⼊第⼆台MySQL服务器。  

前提：服务器上要有docker的环境，我们在docker文件夹下，创建一个mysql0文件夹，创建docker-compose.yml文件

```yml
version: '3.1'
services:
  mysql-0:
    image: mysql
    container_name: mysql-0
    environment:
      MYSQL_ROOT_PASSWORD: root
    command:
      --default-authentication-plugin=mysql_native_password
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_general_ci
      --explicit_defaults_for_timestamp=true
      --lower_case_table_names=1
    ports:
      - 3306:3306
    volumes:
      - ./data:/var/lib/mysql
```

创建名为db_device_0的数据库。

创建物理表 ,==逻辑上tb_device表示的是描述设备信息的表== ，为了体现分表的概念，把tb_device表分成了两张。==于是tb_device就是逻辑表，⽽tb_device_0和tb_device_1就是该逻辑表的物理表。==

- 创建tb_device_0表 

```sql
CREATE TABLE `tb_device_0` (
    `device_id` bigint NOT NULL AUTO_INCREMENT,
    `device_type` int DEFAULT NULL,
    PRIMARY KEY (`device_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3;
```

- 创建tb_device_1表  

```sql
CREATE TABLE `tb_device_1` (
    `device_id` bigint NOT NULL AUTO_INCREMENT,
    `device_type` int DEFAULT NULL,
    PRIMARY KEY (`device_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb3;
```

#### 2.2 创建ShardingSphere项⽬并引⼊依赖  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.wei</groupId>
    <artifactId>shardingJDBC_domo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>shardingJDBC_domo</name>
    <description>shardingJDBC_domo</description>
    <properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <spring-boot.version>2.3.7.RELEASE</spring-boot.version>
    </properties>
    <dependencies>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.16</version>
        </dependency>

        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.1.22</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.0.5</version>
        </dependency>


        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
            <version>4.1.1</version>
        </dependency>


        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

#### 2.3 编写配置⽂件application.properties  

```properties
# 配置真实数据源
spring.shardingsphere.datasource.names=ds1
# 配置第 1 个数据源
spring.shardingsphere.datasource.ds1.type=com.alibaba.druid.pool.DruidD
ataSource
spring.shardingsphere.datasource.ds1.driver-classname=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.ds1.url=jdbc:mysql://172.16.253.84:330
6/db_device_0?serverTimezone=Asia/Shanghai
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=123456
# 配置物理表
spring.shardingsphere.sharding.tables.tb_device.actual-datanodes=ds1.tb_device_$->{0..1}
# 配置分表策略：根据device_id作为分⽚的依据（分⽚键）
spring.shardingsphere.sharding.tables.tb_device.tablestrategy.inline.sharding-column=device_id
spring.shardingsphere.sharding.tables.tb_device.tablestrategy.inline.algorithm-expression=tb_device_$->{device_id%2}
# 开启SQL显示
spring.shardingsphere.props.sql.show = true
```

#### 2.4 创建逻辑表对应的实体  

```java
/**
 * @author huangw
 * @date 2022/8/7 15:59
 */
@Data
public class TbDevice {

    private Long deviceId;

    private int deviceType;
}
```

#### 2.5 使⽤MyBatis-Plus做映射  

```java

/**
 * @author huangw
 * @date 2022/8/7 16:01
 */
@Mapper
public interface DeviceMapper extends BaseMapper<TbDevice> {
}
```

#### 2.6 配置Springboot启动类  

关键是配置MapperScan

```java
@SpringBootApplication
@MapperScan("com.wei.shardingJDBC.mapper")
public class ShardingJdbcDomoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShardingJdbcDomoApplication.class, args);
    }

}
```

#### 2.7 编写单元测试  

尝试插⼊10条device数据，因为分⽚键是`device_id`，且分⽚策略是 `tb_device_$->{device_id%2}` ，因此会根据device_id来决定该条数据会插⼊到哪张物理表中。  

```java
@Test
void initData() {
    for (int i = 0; i < 10; i++) {
        TbDevice device = new TbDevice();
        device.setDeviceId((long) i);
        device.setDeviceType(i);
        deviceMapper.insert(device);
    }
}
```

发现，根据分⽚策略，这10条数据中id是奇数的数据将会被插⼊到tb_device_1表中， id是奇数的数据将会被插⼊到tb_device_0表中。  

## 3、尝试分库分表

#### 3.1 环境准备

使⽤docker创建第⼆台MySQL服务器。  

```yml
version: '3.1'
services:
  mysql-1:
    image: mysql
    container_name: mysql-1
    environment:
      MYSQL_ROOT_PASSWORD: root
    command:
      --default-authentication-plugin=mysql_native_password
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_general_ci
      --explicit_defaults_for_timestamp=true
      --lower_case_table_names=1
    ports:
      - 3306:3306
    volumes:
      - ./data:/var/lib/mysql
```

创建db_device_1数据库。并在数据库中创建两张物理表：  

- 创建tb_device_0表  

```sql
CREATE TABLE `tb_device_0` (
    `device_id` bigint NOT NULL AUTO_INCREMENT,
    `device_type` int DEFAULT NULL,
    PRIMARY KEY (`device_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3;
```

- 创建tb_device_1表  

```sql
CREATE TABLE `tb_device_1` (
    `device_id` bigint NOT NULL AUTO_INCREMENT,
    `device_type` int DEFAULT NULL,
    PRIMARY KEY (`device_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb3;
```

#### 3.2 配置数据库源

提供两个数据源，将之前搭建的两台MySQL服务器作为数据源。  

```properties
# 配置真实数据源
spring.shardingsphere.datasource.names=ds0,ds1
# 配置第 1 个数据源
spring.shardingsphere.datasource.ds0.type=com.alibaba.druid.pool.DruidD
ataSource
spring.shardingsphere.datasource.ds0.driver-classname=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.ds0.url=jdbc:mysql://172.16.253.84:3306/db_device_0?serverTimezone=Asia/Shanghai
spring.shardingsphere.datasource.ds0.username=root
spring.shardingsphere.datasource.ds0.password=123456
# 配置第 2 个数据源
spring.shardingsphere.datasource.ds1.type=com.alibaba.druid.pool.DruidD
ataSource
spring.shardingsphere.datasource.ds1.driver-classname=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.ds1.url=jdbc:mysql://172.16.253.85:3306/db_device_1?serverTimezone=Asia/Shanghai
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=123456
```

#### 3.3 配置数据库表的分⽚策略  

```properties
# 配置分库的分⽚策略，根据device_id进⾏分⽚，奇偶不同进⼊不同的数据库： ds1,ds2
spring.shardingsphere.sharding.default-databasestrategy.inline.sharding-column=device_id
spring.shardingsphere.sharding.default-databasestrategy.inline.algorithm-expression=ds$->{device_id % 2}

# 根据groovy脚本配置数据源+表名
spring.shardingsphere.sharding.tables.tb_device.actual-data-nodes=ds$->{0..1}.tb_device_$->{0..1}

# 配置分表策略
spring.shardingsphere.sharding.tables.tb_device.tablestrategy.inline.sharding-column=device_id
spring.shardingsphere.sharding.tables.tb_device.tablestrategy.inline.algorithm-expression=tb_device_$->{device_id%2}

# 开启显示sql语句
spring.shardingsphere.props.sql.show = true
```

相⽐之前的配置，这次加⼊了两个数据库的分⽚策略，==根据device_id的奇偶特性决定存⼊哪个数据库中==。同时，==使⽤groovy脚本确定了数据库和表之间的关系。==  

```groovy
groovy表达式
ds$->{0..1}.tb_device_$->{0..1}

等效于：
ds0.tb_device_0
ds0.tb_device_1
ds1.tb_device_0
ds1.tb_device_1
```

此时再运⾏测试⽤例，==发现device_id的奇数数据会存⼊ ds1.tb_device_1 表中，偶数数据会存⼊ ds0.tb_device_0 表中。==  

#### 3.4 在分库分表下做查询

```java
@Test
void testQueryById() {
    LambdaQueryWrapper<TbDevice> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(TbDevice::getDeviceId, 1L);
    List<TbDevice> tbDeviceList = deviceMapper.selectList(lambdaQueryWrapper);
    System.out.println(tbDeviceList);
}
```

#### 3.5分库分表存在的问题  

```java
@Test
void testQueryRange() {
    LambdaQueryWrapper<TbDevice> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.between(TbDevice::getDeviceId, 1L, 10L);
    // Inline strategy cannot support this type sharding:RangeRouteValue
    List<TbDevice> tbDeviceList = deviceMapper.selectList(lambdaQueryWrapper);
    System.out.println(tbDeviceList);
}
```

使⽤device_id做范围查询时，发现报错了： ==inline==的分⽚策略没有办法⽀持范围查询。  

```shell
### Cause: java.lang.IllegalStateException: Inline strategy cannot
support this type sharding:RangeRouteValue(columnName=device_id,
tableName=tb_device, valueRange=[1‥10])
```

接下来需要掌握的是分⽚策略 ,为了解决这个问题，出现了不同的分片策略。见第2节。
