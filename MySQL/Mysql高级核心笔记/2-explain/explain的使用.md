# 性能分析工具的使用

## **1、数据库服务器的优化步骤**

当我们遇到数据库调优问题的时候，该如何思考呢？这里把思考的流程整理成下面这张图。

整个流程划分成了` 观察（Show status）` 和 `行动（Action）` 两个部分。字母 **S 的部分代表观察（会使用相应的分析工具）**，**字母 A 代表的部分是行动（对应分析可以采取的行动）。**

![image-20220122161044039](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220122161044039.png)

我们可以通过观察了解数据库整体的运行状态，通过性能分析工具可以让我们了解执行慢的SQL都有哪些，查看具体的SQL执行计划，甚至是SQL执行中的每一步的成本代价，这样才能定位问题所在，找到了问题，再采取相应的行动。

使用三种分析工具进行SQL调优的三个步骤：

`慢查询`,`EXPLAIN`,`SHOW PROFILING`

![image-20220122161519755](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220122161519755.png)

## 2、查看系统性能参数

在MySQL中，可以使用 `SHOW STATUS` 语句查询一些MySQL数据库服务器的 `性能参数 `、 `执行频率` 。 

SHOW STATUS语句语法如下：

```sql
SHOW [GLOBAL|SESSION] STATUS LIKE `参数`
```

一些常用的性能参数如下：

```shell
 • Connections：连接MySQL服务器的次数。

 • Uptime：MySQL服务器的上线时间。

 • Slow_queries：慢查询的次数。

 • Innodb_rows_read：Select查询返回的行数 

• Innodb_rows_inserted：执行INSERT操作插入的行数 

• Innodb_rows_updated：执行UPDATE操作更新的行数 

• Innodb_rows_deleted：执行DELETE操作删除的行数 • Com_select：查询操作的次数。

 • Com_insert：插入操作的次数。对于批量插入的 INSERT 操作，只累加一次。

 • C om_update：更新操作的次数。

 • Com_delete：删除操作的次数
```

## 3、统计SQL的查询成本：last_query_cost

一条SQL查询语句在执行前需要确定查询执行计划，如果存在多种执行计划的话，Mysql会计算每个执行计划所需要的成本，从中选择`成本最小`的一个作为最终执行的执行计划。

如果我们想要查看某条SQL语句的查询成本，可以在执行完这条SQL语句之后，通过查看当前会话中的`last_query_cost`变量值来得到当前查询成本，它通常也是我们`评价一个查询的执行效率`的一个常用指标。这个查询成本对应的是`SQL语句所需要读取的 页的数量 `

我们依然使用第8章的 student_info 表为例：

```sql
CREATE TABLE `student_info` (
  `id` INT (11) NOT NULL AUTO_INCREMENT,
  `student_id` INT NOT NULL,
  `name` VARCHAR (20) DEFAULT NULL,
  `course_id` INT NOT NULL,
  `class_id` INT (11) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8;
```

如果我们想要查询 id=900001 的记录，然后看下查询成本，我们可以直接在聚簇索引上进行查找：

```sql
SELECT student_id, class_id, NAME, create_time FROM student_info
WHERE id = 900001;
```

运行结果（1 条记录，运行时间为 0.042s ）

然后再看下查询优化器的成本，实际上我们只需要检索一个页即可：

![image-20220122163012148](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220122163012148.png)

如果我们想要查询 id 在 900001 到 9000100 之间的学生记录呢？

```sql
SELECT student_id, class_id, NAME, create_time FROM student_info
WHERE id BETWEEN 900001 AND 900100;
```

运行结果（100 条记录，运行时间为 0.046s ）：

然后再看下查询优化器的成本，这时我们大概需要进行 21 个页的查询。

![image-20220122163040720](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220122163040720.png)

你能看到**页的数量**是刚才的 20 倍，但是**查询的效率并没有明显的变化**，实际上这两个 SQL 查询的时间基本上一样，就是==因为采用了顺序读取的方式将页面一次性加载到缓冲池中，然后再进行查找==。虽然 `页 数量（last_query_cost）增加了不少` ，但是**通过缓冲池的机制**，并 `没有增加多少查询时间 `。

==使用场景：==它对于比较开销是非常有用的，特别是我们有好几种查询方式可选的时候。

> SQL查询时一个动态的过程，从页加载的角度来看，我们可以得到以下两点结论：
>
> 1、**位置决定效率**。如果页就在数据库 `缓冲池`中，那么效率是最高的，否则还需要从`内存`或者`磁盘`中进行读取，当然针对单个页的读取来说，如果页存在于内存中，会比在磁盘中读取效率高很多。
>
> 2、**批量决定效率**。如果我们从磁盘中对单一页进行随机读，那么效率是很低的（差不多10ms），而采用顺序读取的方式，批量对页进行读取，平均一页的读取效率就会提升很多，甚至要快于单个页面在内存中的随机读取。
>
> 所以说，遇到I/O并不担心，方法找对了，效率还是很高的。我们首先要考虑数据存放的位置，如果是经常使用的数据就要尽量放到`缓冲池`中，其次我们可以充分利用磁盘的吞吐能力，一次性批量读取数据，这样单个页的读取效率页得到了提升。



## 4、定位执行慢的SQL：慢查询日志

MySQL的慢查询日志，用来记录在Mysql中`响应时间超过阈值`的语句，具体指运行时间超过`long_query_time`值得SQL，则会被记录到慢查询日志中，long_query_time得默认值是10，意思是运行10s以上（不包含10s）得语句，认为是超出了我们最大忍耐时间。

它得主要作用是，帮住我们发现那些执行时间特别长得SQL语句，并且有针对性的进行优化，从而提高系统的整体效率。当我们的数据库服务器发生阻塞，运行变慢的时候，检查一下慢查询日志，找到那些慢查询，对解决问题很有帮住。比如一条SQL执行超过5m，我们就算慢SQL，希望能收集超过5s的sql，结合explain进行全面分析。

默认情况下，Mysql数据库`没有开启慢查询日志`，需要我们手动来设置这个参数，==如果不是调优需要的话，不建议启动该参数，因为开启慢查询日志会或多或少带来一定的性能影响。==

慢查询日志支持将日志记录写入文件。

#### 4.1 开启慢查询日志参数

**1、开启slow_query_log**

在使用前，我们需要先看下慢查询是否已经开启，使用下面命令即可

```sql
show variables like '%slow_query_log'

set global slow_query_log='ON';
```

![image-20220122172759256](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220122172759256.png)

你能看到这时慢查询分析已经开启，同时文件保存在 `/var/lib/mysql/atguigu02-slow.log` 文件中。

**2、修改long_query_time阈值**

接下来我们来看下慢查询的时间阈值设置，使用如下命令：

```sql
show variables like '%long_query_time%';
```

![image-20220122172952252](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220122172952252.png)

这里如果我们想把时间缩短，比如设置为 1 秒，可以这样设置：

```sql
#测试发现：设置global的方式对当前session的long_query_time失效。对新连接的客户端有效。所以可以一并执行下述语句 

set global long_query_time = 1; 

show global variables like '%long_query_time%'; 

set long_query_time=1; 

show variables like '%long_query_time%';
```

**补充：配置文件中一并设置参数**

如下的方式相较于前面的命令行方式，可以看坐是永久设置的方式。

修改`my.cnf`文件，【mysqld】下增加或修改参数`long_query_time`,`slow_query_log`和`slow_query_log_file`后，然后重启Mysql服务器。

```properties
[mysqld]
slow_query_log=ON #开启慢查询日志开关
slow_query_log_file=/var/lib/mysql/hostname-slow.log #慢查询日志的目录和文件名信息
long_query_time=3 #设置慢查询值为3s，超出此设定SQL即被记录到慢查询日志
log.output=FILE
```

如果不指定存储路径，慢查询日志将默认存储到Mysql数据库的数据文件夹下。如果不指定文件名，默认文件名为hostname-slow.log

#### **4.2** **查看慢查询数目**

查询当前系统中有多少条慢查询记录

```sql
SHOW GLOBAL STATUS LIKE '%slow_queries%';
```

> 补充说明：
>
> 除了上述变量，控制慢查询日志的还有一个系统变量：min_examined_row_limit。这个变量的意思是，查询`扫描过的最少记录数`。这个变量和查询执行时间，共同组成了判别一个查询是否是慢查询的条件。
>
> 如果查询扫描过的记录数>=这个变量的值，并且查询执行时间超过long_query_time的值，那么，这个查询就被记录到慢查询日志中；反之，则不会被记录到慢查询日志中。
>
> show variables like 'min%'
>
> 这个值默认是0，与long_query_time=10结合在一起，表示只要查询的执行时间超过10秒，哪怕一个记录也没有扫描过，都要被记录到慢查询日志中，你也可以根据需要，通过修改`my.ini conf`文件来修改查询的扫描行数，或者通过SET指令，用SQL语句来修改min_examined_row_limit的值。

#### 4.3 慢查询日志分析工具：mysqldumpslow

在生产环境中，如果要手工分析日志，查找、分析SQL，显然是个体力活，MySQL提供了日志分析工具`mysqldumpslow` 。

查看mysqldumpslow的帮助信息

`mysqldumpslow --help`

![image-20220122174827463](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220122174827463.png)

mysqldumpslow 命令的具体参数如下：

![image-20220122175417868](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220122175417868.png)

举例：我们想要按照查询时间排序，查看前五条 SQL 语句，这样写即可：

```sql
mysqldumpslow -s t -t 5 /var/lib/mysql/atguigu01-slow.log
```

![image-20220122175452590](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220122175452590.png)

**工作常用参考：**

```sql
#得到返回记录集最多的10个SQL 
mysqldumpslow -s r -t 10 /var/lib/mysql/atguigu-slow.log

#得到访问次数最多的10个SQL 
mysqldumpslow -s c -t 10 /var/lib/mysql/atguigu-slow.log

#得到按照时间排序的前10条里面含有左连接的查询语句 
mysqldumpslow -s t -t 10 -g "left join" /var/lib/mysql/atguigu-slow.log

#另外建议在使用这些命令时结合 | 和more 使用 ，否则有可能出现爆屏情况 
mysqldumpslow -s r -t 10 /var/lib/mysql/atguigu-slow.log | more
```

#### **4.4** **关闭慢查询日志** 

MySQL服务器停止慢查询日志功能有两种方法：

**方式1：永久性方式**

```properties
[mysqld] 
slow_query_log=OFF
```

或者，把slow_query_log一项注释掉 或 删除

```properties
[mysqld] 
slow_query_log =OFF 
```

重启MySQL服务，执行如下语句查询慢日志功能。

```sql
SHOW VARIABLES LIKE '%slow%'; #查询慢查询日志所在目录

SHOW VARIABLES LIKE '%long_query_time%'; #查询超时时长
```

**方式2：临时性方式**

使用SET语句来设置。 （1）停止MySQL慢查询日志功能，具体SQL语句如下。

```sql
SET GLOBAL slow_query_log=off;
```

（2）**重启MySQL服务**，使用SHOW语句查询慢查询日志功能信息，具体SQL语句如下

```sql
SHOW VARIABLES LIKE '%slow%';
#以及
SHOW VARIABLES LIKE '%long_query_time%';
```

#### 4.5 删除慢查询日志

使用SHOW语句显示慢查询日志信息，具体SQL如下。

```sql
show variables like 'slow_query_log%'；
```

![image-20220122175959877](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220122175959877.png)

从执行结果可以看出，慢查询日志的目录默认为mysql的数据目录，在该目录下`手动删除慢查询日志文件`即可。

使用命令`mysqladmin flush-logs`来重新生成查询日志文件，具体命令如下，执行完毕会在数据目录下重新生成慢查询日志文件。

```sql
mysqladmin -uroot -p flush-logs slow
```

> 提示：慢查询日志都是使用mysqladmin flush-logs命令来删除重建的。使用时一定要注意，一旦执行了这个命令，慢查询日志都只存储到新的日志文件中，如果需要旧的查询日志，就必须事先备份。



## 5、查看SQL执行成本

```sql
show variables like 'profiling';
```

![image-20220123003655697](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220123003655697.png)

通过设置` profiling='ON’ `来开启 show profile：

```sql
mysql > set profiling = 'ON';
```

![image-20220123003721970](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220123003721970.png)

然后执行相关的查询语句。接着看下当前会话都有哪些 profiles，使用下面这条命令：

```sql
mysql > show profiles;
```

![image-20220123003737932](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220123003737932.png)

你能看到当前会话一共有 2 个查询。如果我们想要查看最近一次查询的开销，可以使用：

```sql
mysql > show profile;
```

![image-20220123003756777](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220123003756777.png)

```sql
mysql> show profile cpu,block io for query 2;
```

![image-20220123003810021](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220123003810021.png)

show profile的常用查询参数:

- ALL:显示所有开销信息
- BLOCK IO：显示块IO开销
- CONTEXT SWITCHES：上下文切换开销
- CPU：显示CPU开销信息
- IPC：显示发送和接收开销信息
- MEMORY：显示内存开销信息
- PAGE FAULTS：显示页面错误开销信息
- SOURCE：显示Source_function，Source_file,Source_line相关开销信息
- SWAPS:显示交换次数开销信息

==日常开发需要注意的结论：==

**1、`converting help to myisam`：查询结果太大，内存不够，数据往磁盘上搬了**

**2、`creating tmp table`：创建临时表，先拷贝数据到临时表，用完后再删除临时表**

**3、`copying to tmp table on disk`：把内存中临时表复制到磁盘上，警惕。**

**4、`locked ` 被锁定**

**如果再show profile诊断结果中出现了以上四条结论中的任何一条， 则sql语句需要优化。**

**注意：不过show profile命令将被弃用，我们可以从 information_schema中的profiling数据表去进行查看。**



## 6、分析查询语句：EXPLAIN

#### 6.1 基本语法

````sql
EXPLAIN SELECT select_options
````

如果我们想看看某个查询的执行计划的话，可以在具体的查询语句前边加一个 EXPLAIN ，就像这样：

```sql
mysql> EXPLAIN SELECT 1
```

EXPLAIN 语句输出的各个列的作用如下：

![image-20220123170127725](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220123170127725.png)

#### 6.2 准备数据

**1、建表**

```sql
CREATE TABLE s1 (
	id INT AUTO_INCREMENT,
	key1 VARCHAR ( 100 ),
	key2 INT,
	key3 VARCHAR ( 100 ),
	key_part1 VARCHAR ( 100 ),
	key_part2 VARCHAR ( 100 ),
	key_part3 VARCHAR ( 100 ),
	common_field VARCHAR ( 100 ),
	PRIMARY KEY ( id ),
	INDEX idx_key1 ( key1 ),
	UNIQUE INDEX idx_key2 ( key2 ),
	INDEX idx_key3 ( key3 ),
INDEX idx_key_part ( key_part1, key_part2, key_part3 ) 
) ENGINE = INNODB CHARSET = utf8;
```

```sql
CREATE TABLE s2 (
	id INT AUTO_INCREMENT,
	key1 VARCHAR ( 100 ),
	key2 INT,
	key3 VARCHAR ( 100 ),
	key_part1 VARCHAR ( 100 ),
	key_part2 VARCHAR ( 100 ),
	key_part3 VARCHAR ( 100 ),
	common_field VARCHAR ( 100 ),
	PRIMARY KEY ( id ),
	INDEX idx_key1 ( key1 ),
	UNIQUE INDEX idx_key2 ( key2 ),
	INDEX idx_key3 ( key3 ),
	INDEX idx_key_part ( key_part1, key_part2, key_part3 ) 
) ENGINE = INNODB CHARSET = utf8;
```

**2、设置参数 log_bin_trust_function_creators**

创建函数，假如报错，需开启如下命令：允许创建函数设置：

```sql
set global log_bin_trust_function_creators=1; # 不加global只是当前窗口有效。
```

**3、创建函数**

```sql
DELIMITER //
CREATE FUNCTION rand_string1 ( n INT ) RETURNS VARCHAR ( 255 ) #该函数会返回一个字符串
BEGIN
	DECLARE
		chars_str VARCHAR ( 100 ) DEFAULT 'abcdefghijklmnopqrstuvwxyzABCDEFJHIJKLMNOPQRSTUVWXYZ';
	DECLARE
		return_str VARCHAR ( 255 ) DEFAULT '';
	DECLARE
		i INT DEFAULT 0;
	WHILE
			i < n DO
			
			SET return_str = CONCAT(
				return_str,
			SUBSTRING( chars_str, FLOOR( 1+RAND ()* 52 ), 1 ));
		
		SET i = i + 1;
		
	END WHILE;
	RETURN return_str;
	
END // 
DELIMITER;
```

**4、创建存储过程**

创建往s1表中插入数据的存储过程：

```sql
DELIMITER //
CREATE PROCEDURE insert_s1 (
	IN min_num INT ( 10 ),
	IN max_num INT ( 10 )) BEGIN
	DECLARE
		i INT DEFAULT 0;
	
	SET autocommit = 0;
	REPEAT
			
			SET i = i + 1;
		INSERT INTO s1
		VALUES
			(
				( min_num + i ),
				rand_string1 ( 6 ),
				( min_num + 30 * i + 5 ),
				rand_string1 ( 6 ),
				rand_string1 ( 10 ),
				rand_string1 ( 5 ),
				rand_string1 ( 10 ),
			rand_string1 ( 10 ));
		UNTIL i = max_num 
	END REPEAT;
	COMMIT;
	
END // 
DELIMITER;
```

创建往s2表中插入数据的存储过程：

```sql

DELIMITER //
CREATE PROCEDURE insert_s2 (
	IN min_num INT ( 10 ),
	IN max_num INT ( 10 )) BEGIN
	DECLARE
		i INT DEFAULT 0;
	
	SET autocommit = 0;
	REPEAT
			
			SET i = i + 1;
		INSERT INTO s2
		VALUES
			(
				( min_num + i ),
				rand_string1 ( 6 ),
				( min_num + 30 * i + 5 ),
				rand_string1 ( 6 ),
				rand_string1 ( 10 ),
				rand_string1 ( 5 ),
				rand_string1 ( 10 ),
			rand_string1 ( 10 ));
		UNTIL i = max_num 
	END REPEAT;
	COMMIT;
	
END // 
DELIMITER;
```

**5、调用存储过程**

```sql
CALL insert_s1(10001,10000);

CALL insert_s2(10001,10000);

```

#### 6.3 EXPLAIN各列的作用

##### 1、table

不论我们的查询语句有多复杂，里面包含了多少个表，到最后也是需要对每个表进行`单表访问`的，所以MYSQL规定==EXPLAIN语句输出的每条记录都对应着某个单表的访问方法==，该条记录的table列代表着该表的表名（有时不是真实的表名字，可能是简称）

```sql
EXPLAIN select * from s1;
```

![image-20220123180559997](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220123180559997.png)

这个查询语句只涉及对s1表的单表查询，所以`EXPLAIN`输出只有1条记录，其中的table列的值是s1，表明这条记录是用来说明对s1表的单表访问方法的。

下边我们看一个连接查询的执行计划：

```sql
EXPLAIN select * from s1 inner join s2;
```

![image-20220123183252069](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220123183252069.png)

##### 2、Id

我们写的查询语句一般都是以`select`关键字开头，比较简单的查询语句里只有一个`select`关键字，比如下边这个查询语句：

```sql
SELECT * FROM s1 WHERE key1 = 'a';
```

稍微复杂一点的连接查询中也只有一个 SELECT 关键字，比如：

```sql
SELECT * FROM s1 INNER JOIN s2
ON s1.key1 = s2.key1
WHERE s1.common_field = 'a';
```

```sql
EXPLAIN SELECT * FROM s1 WHERE key1 = 'a';
```

![image-20220123183709306](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220123183709306.png)

```sql
# 查询优化器可能对涉及子查询的查询语句进行重写，转变为多表查询的操作 
EXPLAIN SELECT * FROM s1 WHERE key1 IN (SELECT key2 FROM s2 WHERE common_field
= 'a');
```

![image-20220123190740814](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220123190740814.png)

```sql
# union 会默认去重，union all 不会
EXPLAIN SELECT * FROM s1 UNION SELECT * FROM s2;
```

![image-20220123191136149](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220123191136149.png)

```sql
EXPLAIN SELECT * FROM s1 UNION ALL SELECT * FROM s2;
```

![image-20220123191159743](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220123191159743.png)

小结:

- id如果相同，可以认为是一组，从上往下顺序执行
- 在所有组中，id值越大，优先级越高，越先执行
- 关注点：id号每个号码，表示一趟独立查询，一个sql的查询趟数越少越好。

##### 3、select_type ✳

一条大的查询语句里边可以包含若干个SELECT关键字，`每个SELECT关键字代表着一个小的查询语句`，而每个SELECT关键字的FROM字句中，都可以包含若干张表（这些表是用来做连接查询的），`每一张表都对应着执行计划输出中的一条记录`，对于在同一个SELECT关键字中的表来说，它们的id是相同的。

MYSQL为每一个关键字代表的小查询定义了一个称之为`select_type`属性，意思是我们只要知道了某个小查询的`select_types`属性，就知道了这个`小查询在整个大查询中扮演了一个什么角色`，我们看一下，select_type都能取哪些值。

![image-20220124083635845](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124083635845.png)

具体分析如下：

```sql
EXPLAIN SELECT * FROM s1;
```

![image-20220124083806018](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124083806018.png)

当然，连接查询也算是 SIMPLE 类型，比如:

```sql
EXPLAIN SELECT * FROM s1 INNER JOIN s2;
```

![image-20220124083826630](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124083826630.png)

> PRIMARY

```sql
EXPLAIN SELECT * FROM s1 UNION SELECT * FROM s2;
```

![image-20220124083927951](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124083927951.png)

> SUBQUERY

```sql
EXPLAIN SELECT * FROM s1 WHERE key1 IN (SELECT key1 FROM s2) OR key3 = 'a';
```

![image-20220124083958683](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124083958683.png)

> DEPENDENT SUBQUERY 

```sql
EXPLAIN SELECT * FROM s1 WHERE key1 IN (SELECT key1 FROM s2 WHERE s1.key2 = s2.key2) OR key3 = 'a';
```

![image-20220124084023966](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124084023966.png)

> DEPENDENT UNION 

```sql
EXPLAIN SELECT * FROM s1 WHERE key1 IN (SELECT key1 FROM s2 WHERE key1 = 'a' UNION SELECT key1 FROM s1 WHERE key1 = 'b');
```

![image-20220124084043300](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124084043300.png)

> DERIVED

```sql
 EXPLAIN SELECT * FROM (SELECT key1, count(*) as c FROM s1 GROUP BY key1) AS derived_s1 where c > 1;
```

![image-20220124084105929](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124084105929.png)

> MATERIALIZED

```sql
 EXPLAIN SELECT * FROM s1 WHERE key1 IN (SELECT key1 FROM s2);
```

![image-20220124084142531](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124084142531.png)

##### 4、type✳

完整的访问方法如下：` system `、` const `、` eq-ref `, `ref `,` fulltext `,` ref-or-null `, ` index-merge `,` unique-subquery `,` index-subquery `, ` range `, ` index `,` ALL `

我们详细解释一下：

- **system**

当**表中只有一条记录并且该表使用的存储引擎的统计数据是精确**的，比如MyIsam，Mermory，那么对该表的访问方法就是system，比方说我们新建一个表，插入一条数据。

```sql
CREATE TABLE t(i int) Engine=MyISAM;

INSERT INTO t VALUES(1);
```

然后我们看一下查询这个表的执行计划：

```sql
EXPLAIN SELECT * FROM t;
```

![image-20220124085223270](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124085223270.png)

- **const**

**当我们根据主键或者唯一索引、二级索引与常数进行等值匹配的时候，对单表的访问方法就是 const**

```sql
EXPLAIN SELECT * FROM s1 WHERE id = 10005;
```

![image-20220124085252611](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124085252611.png)

- **eq_ref**

在连接查询时，**如果被驱动表时通过主键或者唯一索引、二级索引等值匹配的方式进行访问的（如果该主键或者二级索引是联合索引的华，所有索引列都必须进行等值比较），则对该对该驱动表的访问方法就是 eq_ref**

```sql
EXPLAIN SELECT * FROM s1 INNER JOIN s2 ON s1.id = s2.id;
```

![image-20220124085317331](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124085317331.png)

从执行计划的结果中可以看出，**MySQL打算将s2作为驱动表，s1作为被驱动表**，重点关注s1的访问方法是 eq_ref ，表明在访问s1表的时候可以 通过主键的等值匹配 来进行访问。

- **ref**

**当通过普通的二级索引与常量进行等值匹配时来查询某个表，那么对表的访问方法就是ref**

```sql
 EXPLAIN SELECT * FROM s1 WHERE key1 = 'a';
```

![image-20220124085412155](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124085412155.png)

- **ref_or_null**

**当对普通二级索引进行等值匹配查询，该索引列也可以是NULL时，那么对该表的访问方法就可能是ref_or_null**

```sql
EXPLAIN SELECT * FROM s1 WHERE key1 = 'a' OR key1 IS NULL;
```

![image-20220124085439903](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124085439903.png)

- **index_merge** 

```sql
EXPLAIN SELECT * FROM s1 WHERE key1 = 'a' OR key3 = 'a';
```

![image-20220124085507076](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124085507076.png)

从执行计划的 `type `列的值是 `index_merge `就可以看出，MySQL 打算使用索引合并的方式来执行对 s1 表的查询

- **unique_subquery** 

**如果针对一些IN子查询语句中，查询优化器决定将IN子查询转换成EXISTS子查询，而且子查询可以使用主键进行等值匹配的华，那么该子查询的执行计划的type就是unique_subquery**

```sql
EXPLAIN SELECT * FROM s1 WHERE key2 IN (SELECT id FROM s2 where s1.key1 = s2.key1) OR key3 = 'a';
```

![image-20220124085556102](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124085556102.png)

- **index_subquery**

```sql
EXPLAIN SELECT * FROM s1 WHERE common_field IN (SELECT key3 FROM s2 where s1.key1 = s2.key1) OR key3 = 'a';
```

![image-20220124085623681](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124085623681.png)

- **range**

**如果使用索引获取某些范围区间的记录，那么久可能使用到range访问方法**

```sql
EXPLAIN SELECT * FROM s1 WHERE key1 IN ('a', 'b', 'c');
```

![image-20220124085653653](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124085653653.png)

- **index**

**当我们可以使用索引覆盖，但需要扫描全部的索引记录时，该表的访问方法就是index**

```sql
EXPLAIN SELECT key_part2 FROM s1 WHERE key_part3 = 'a';
```

![image-20220124085722888](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124085722888.png)

- ALL

全表扫描type=all

```sql
EXPLAIN SELECT * FROM s1;
```

![image-20220124085743510](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124085743510.png)

结果值从最好到最坏依次是：

> **system >  const  >  eq-ref  > ref** >
>
> fulltext > ref_or_null > index_merge > unique_subquery > index_subquery >
>
> **range > index > ALL**

==**SQL性能优化的目标：至少要达到 range级别，要求是 ref 级别，最好是 consts级别。（阿里巴巴开发手册要求）**==

##### 5、possible_key 和 key

possible_key ： 可能使用到的索引

key：真正使用的索引

```sql
EXPLAIN SELECT * FROM s1 WHERE key1 > 'z' AND key3 = 'a';
```

![image-20220124091129038](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124091129038.png)

##### 6、key_len✳

**实际使用到的索引长度（字节数）**

**帮你检查是否充分的利用上了索引 ，值是越大越好**

==主要针对联合索引，有一定的参考意义==

```sql
EXPLAIN SELECT * FROM s1 WHERE id = 10005;
```

![image-20220124091246578](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124091246578.png)

```sql
EXPLAIN SELECT * FROM s1 WHERE key2 = 10126;
```

![image-20220124091822179](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124091822179.png)

```sql
EXPLAIN SELECT * FROM s1 WHERE key1 = 'a';
```

![image-20220124091939462](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124091939462.png)

```sql
EXPLAIN SELECT * FROM s1 WHERE key_part1 = 'a';
```

![image-20220124091952637](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124091952637.png)

```sql
EXPLAIN SELECT * FROM s1 WHERE key_part1 = 'a' AND key_part2 = 'b';
```

![image-20220124092008353](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124092008353.png)

##### 7、ref

**当使用索引列等值查询时，与索引列进行等值匹配的对象信息**

```sql
EXPLAIN SELECT * FROM s1 WHERE key1 = 'a';
```

![image-20220124092323736](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124092323736.png)

```sql
EXPLAIN SELECT * FROM s1 INNER JOIN s2 ON s1.id = s2.id;
```

![image-20220124092342301](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124092342301.png)

```sql
EXPLAIN SELECT * FROM s1 INNER JOIN s2 ON s2.key1 = UPPER(s1.key1);
```

![image-20220124093105568](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124093105568.png)

##### 8、**rows**✳

**预估的需要读取的记录条数（扫描行数）**，值越小越好

```sql
EXPLAIN SELECT * FROM s1 WHERE key1 > 'z';
```

![image-20220124093144576](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124093144576.png)

##### **9、filtered** 

**某个表经过搜索条件过滤后剩余记录条数的百分比**

**如果使用的是索引执行的单表扫描，那么计算时需要估计出满足除使用索引，到对应索引的搜索条件外的其他搜索条件的记录有多少条。。**

```sql
 EXPLAIN SELECT * FROM s1 WHERE key1 > 'z' AND common_field = 'a';
```

![image-20220124124909633](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124124909633.png)

```sql
EXPLAIN SELECT * FROM s1 INNER JOIN s2 ON s1.key1 = s2.key1 WHERE s1.common_field = 'a';
```

![image-20220124124942982](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124124942982.png)

##### 10、Extra✳

额外补充的信息，包含不适合在其他列中显示但是十分重要的额外信息。我们可以通过这些额外信息来更准确的理解Mysql到底时如何执行给定的查询语句。Mysql提供的额外信息有好几十个，我们就不一一介绍了，所以我们只调比较重要的额外信息介绍给大家。

- NO table used

当查询语句没有FROM自居时将会提示该信息

```sql
EXPLAIN select 1;
```

- Impossible WHERE

查询语句的WHERE子句永远为false时将会提示该额外信息

```sql
EXPLAIN SELECT * FROM s1 WHERE 1 != 1;
```

![image-20220124125409622](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124125409622.png)

- Using where

当我们使用全表扫描来执行某个表的查询，并且该语句的where字句中有针对该表的搜索条件时，在Extra列中会提示上述额外信息。

```sql
 EXPLAIN SELECT * FROM s1 WHERE common_field = 'a';
```

![image-20220124125617432](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124125617432.png)

- Using index

当我们的查询列表以及搜索条件中只包含属于某个索引的列，也就是可以使用覆盖索引的情况下。在EXTRA列中会提示该额外信息。

比如说这个查询中需要用到`idx_key1`而不需要回表操作

```sql
EXPLAIN SELECT key1 FROM s1 WHERE key1 = 'a';
```

![image-20220124125824989](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124125824989.png)

- using index conditions

有些搜索条件中虽然出现了索引列，但却不能使用到索引，这就是索引下推

```sql
EXPLAIN SELECT * FROM s1 WHERE key1 > 'z' AND key1 LIKE '%b';
```

![image-20220124125959731](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124125959731.png)



- Using join buffer (Block Nested Loop) 

当被驱动表不能有效的利用索引来加快访问速度，Mysql一般会为其分配一块名叫`join buffer`的内存块来加快查询速度，也就是网门讲的`基于块的嵌套循环算法`

```sql
EXPLAIN SELECT * FROM s1 INNER JOIN s2 ON s1.common_field = s2.common_field;
```

![image-20220124130203314](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124130203314.png)

- Using filesort

如果某个查询需要使用文件排序的方式进行查询，就会在执行计划中显示Using filesort提示

```sql
EXPLAIN SELECT * FROM s1 ORDER BY key1 LIMIT 10;
```

![image-20220124130418422](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124130418422.png)

- Using temporary

Mysql借助临时表来完成一些功能，比如去重，排序之类的。

如果在带有`distinct`,`group by`,`union`等字句查询过程中，如果不能有效的利用索引来完成查询，mysql很有可能会去建立内部临时表来执行查询，查询中如果使用到了内部的临时表，在效率上会有影响。额外信息中出现Using temporary

```sql
EXPLAIN SELECT DISTINCT common_field FROM s1;
```

![image-20220124130753376](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220124130753376.png)

#### 6.4 小结

- EXPLAIN不考虑各种Cache
- EXPLAIN不能显示MySQL在执行查询时所作的优化工作
- EXPLAIN不会告诉你关于触发器、存储过程的信息或用户自定义函数对查询的影响情况
- 部分统计信息是估算的，并非精确值

## 7、EXPLAIN的进一步使用

#### 7.1 EXPLAIN的四种输出格式

这里谈谈EXPLAIN的输出格式。EXPLAIN可以输出四种格式： 传统格式 ， JSON格式 ， TREE格式 以及 可视化输出 。用户可以根据需要选择适用于自己的格式。

一般采用传统格式即可，json格式会携带更多的信息。

#### 7.2 SHOWWARINGS的使用

查看Mysql优化器优化后的语句，默认会查看上一条执行的SQL被优化后的SQL

```sql
EXPLAIN SELECT s1.key1, s2.key1 FROM s1 LEFT JOIN s2 ON s1.key1 = s2.key1 WHERE s2.common_field IS NOT NULL;
```

![image-20220125102914117](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220125102914117.png)

```mysql
mysql> SHOW WARNINGS\G *************************** 1. row *************************** Level: Note Code: 1003 Message: /* select#1 */ select `atguigu`.`s1`.`key1` AS `key1`,`atguigu`.`s2`.`key1` AS `key1` from `atguigu`.`s1` join `atguigu`.`s2` where ((`atguigu`.`s1`.`key1` = `atguigu`.`s2`.`key1`) and (`atguigu`.`s2`.`common_field` is not null)) 1 row in set (0.00 sec)
```

## 8、分析优化器执行计划：trace

```sql
SET optimizer_trace="enabled=on",end_markers_in_json=on;

set optimizer_trace_max_mem_size=1000000;
```

开启以后，可分析如下语句：

```
SELECT   INSERT  DELETE
REPLACE  UPDATE  EXPLAIN
SET      DECLARE  CASE
IF   RETURN    CALL
```

测试：执行如下SQL语句

```sql
select * from student where id < 10;
```

最后， 查询 information_schema.optimizer_trace 就可以知道MySQL是如何执行SQL的

```mysql
select * from information_schema.optimizer_trace\G
```

![image-20220125103428694](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220125103428694.png)



![image-20220125103452964](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220125103452964.png)

![image-20220125103506197](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220125103506197.png)

![image-20220125103519218](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220125103519218.png)

## 9、Mysql监控分析视图-sys schema

#### 9.1 视图摘要

**1.** **主机相关：**以host_summary开头，主要汇总了IO延迟的信息。

**2. Innodb****相关：**以innodb开头，汇总了innodb buffer信息和事务等待innodb锁的信息。

**3. I/o****相关：**以io开头，汇总了等待I/O、I/O使用量情况。

**4.** **内存使用情况：**以memory开头，从主机、线程、事件等角度展示内存的使用情况

**5.** **连接与会话信息：**processlist和session相关视图，总结了会话相关信息。

**6.** **表相关：**以schema_table开头的视图，展示了表的统计信息。

**7.** **索引信息：**统计了索引的使用情况，包含冗余索引和未使用的索引情况。

**8.** **语句相关：**以statement开头，包含执行全表扫描、使用临时表、排序等的语句信息。

**9.** **用户相关：**以user开头的视图，统计了用户使用的文件I/O、执行语句统计信息。

**10.** **等待事件相关信息：**以wait开头，展示等待事件的延迟情况。



#### **9.2 Sys schema视图使用场景**

**索引情况**

```sql
#1. 查询冗余索引 
select * from sys.schema_redundant_indexes; 

#2. 查询未使用过的索引 
select * from sys.schema_unused_indexes; 

#3. 查询索引的使用情况 
select index_name,rows_selected,rows_inserted,rows_updated,rows_deleted from sys.schema_index_statistics where table_schema='dbname' ;
```

**表相关**

```sql
# 1. 查询表的访问量 
select table_schema,table_name,sum(io_read_requests+io_write_requests) as io from sys.schema_table_statistics group by table_schema,table_name order by io desc; 

# 2. 查询占用bufferpool较多的表 
select object_schema,object_name,allocated,data from sys.innodb_buffer_stats_by_table order by allocated limit 10; 

# 3. 查看表的全表扫描情况 
select * from sys.statements_with_full_table_scans where db='dbname';
```

**语句相关**

```sql
#1. 监控SQL执行的频率 
select db,exec_count,query from sys.statement_analysis order by exec_count desc;

#2. 监控使用了排序的SQL
select db,exec_count,first_seen,last_seen,query from sys.statements_with_sorting limit 1;

#3. 监控使用了临时表或者磁盘临时表的SQL
select db,exec_count,tmp_tables,tmp_disk_tables,query
from sys.statement_analysis where tmp_tables>0 or tmp_disk_tables >0 order by (tmp_tables+tmp_disk_tables) desc;
```

**IO相关**

```sql
#1. 查看消耗磁盘IO的文件 
select file,avg_read,avg_write,avg_read+avg_write as avg_io
from sys.io_global_by_file_by_bytes order by avg_read limit 10;
```

**Innodb相关**

```sql
#1. 行锁阻塞情况 
select * from sys.innodb_lock_waits;
```

