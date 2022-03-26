# MySQL事务日志

事务有4种特性：原子性、一致性、隔离性和持久性。那么事务的四种特性到底是基于什么机制实现呢？

- 事务的**隔离性**由`锁机制`实现
- 而**事务的原子性、一致性和持久性**由事务的**redo日志**和**undo日志**来保证
  - redo log 称为 `重做日志`，提供再写入操作，恢复提交事务修改的页操作，用来保证事务的持久性。
  - undo log 称为 `回滚日志`，回滚行记录到某个特定版本，用来保证事务的原子性、一致性。

有的DBA或许会认为undo是redo的逆过程，其实不然。redo和undo都可以视为是一种`恢复操作`，但是：

- redo log ：是存储引擎层（innodb）生成的日志，记录的是`物理级别`上的页修改操作，比如页号xxx，偏移量yyy，写了zzz数据，主要为了保证数据的可靠性。
- undo log：是存储引擎（innodb）生成的日志，记录的是`逻辑操作`的日志。比如对某一行数据进行了insert语句操作，那么undo log就记录一条与之相反的delete操作。主要用于`事务的回滚`（undo log 记录的是每个修改操作的**逆日志**）和**一致性非锁定读**（==undo log回滚行记录到某种特定版本--MVCC==，即多版本并发控制）

## 1、redo日志

#### 1.1 为什么需要redo日志

一方面，缓冲池可以帮助我们消除cpu和磁盘间的鸿沟，checkpoint机制可以保证数据的最终落盘，然而由于checkpoint`并不是每次变更的时候就会触发`的，而是master线程隔一段时间就去处理的。==所以最坏的情况就是事务提交后，刚写完缓冲池，数据库宕机了，那么这段数据就是丢失的，无法恢复==。

另一方面，==事务包含`持久性`的特性，就是说对于一个已经提交的事务，在事务提交后即使系统发生了崩溃，这个事务对数据库种所做的更改也不能丢失==。

那么如何保证这个持久性呢？

一个`简单的做法`：**在事务提交完成之前，该事务所修改的所有页面都刷新到磁盘**，但是这个简单粗暴的做法有些问题。

- **修改量与刷新磁盘工作量严重不成比例**

有时候我们仅仅修改了某个页面中的一个字节，但是我们知道在innodb中是以页为单位来进行磁盘IO的，也就是说我们在该事务提交时不得不将一个完整的页面从内存中刷新到磁盘，我们又知道一个页面默认是16kb，只修改一个字节就要刷新16kb的数据到磁盘上显然是小题大做了。

- **随机io刷新较慢**

一个事务可能包含很多语句，即使一条语句也可能修改许多数据页，假如该事务修改的这些页面可能并不相邻，这就意味着在将某个事务修改的buffer pool中的页面`刷新到磁盘时`，需要进行很多的`随机IO`，随机IO比顺序IO要慢，尤其对于传统的机械硬盘来说。

`另一个解决思路：` 我们只是想让已经提交了的事务对数据库中数据所做的修改永远生效，即使后来系统崩溃，在重启后也能把这种修改恢复出来。**所以我们其实没有必要记录在每次事务提交时，就把该事务在内存中修改过的==全部页==刷新到磁盘。**只需要把`修改`了哪些东西`记录一下`就好。

例如：某个事务将系统表空间中`第10号`页面中偏移量为`100处`的那个字节的值`1`改成`2`。

我们只需要记录一下：将0号表空间的10号页面的偏移量为100处的值更新为2。

![image-20220129230321191](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220129230321191.png)

innoDB引擎的事务采用了`WAL技术`（**write-ahead logging**），这种技术的思想就是先写日志，再写磁盘，只有日志写入成功，才算事务提交成功，这里的日志就是redo log。当发生宕机且数据未刷到磁盘的时候，可以通过redo log来恢复，保证ACID中的D（持久性）,这就是redo的作用。

#### 1.2 redo日志的好处、特点

**1、好处**

- **redo日志降低了刷盘频率**
- **redo日志占用的空间非常小**

存储表空间ID、页号、偏移量以及需要更新的值，所需的存储空间是很小的，刷盘快。

**2、特点**

- **redo日志是顺序写入磁盘的**

在执行事务的过程中，每执行一条语句，就可能产生若干条redo日志，这些日志是按照`产生的顺序写入磁盘的`，也就是使用顺序IO，效率比随机IO快。

- **事务执行过程中，redo log不断记录**

redo log 跟bin log的区别，redo log 是存储引擎层产生的。而bin log是数据库层次产生的。假设一个事务，对表做10w行的记录插入，在这个过程中，一直不断的往redo log顺序记录，而bin log不会记录，直到这个事务提交，才会一次写入到bin log文件中。

#### 1.3 redo的组成

redo log可以简单分成以下两部分

- `重做日志的缓冲（redo log buffer )`，保存在**内存**中，是易失的。

在服务器启动时就向操作系统申请了一大片称之为redo log buffer的`连续内存`空间，翻译成中文就是redo日志缓冲区。这片内存空间被划分成若干个连续的`redo log block`。一个redo log block占用`512字节`大小。

![image-20220209191142965](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220209191142965.png)

**参数设置：innodb_log_buffer_size：**

redo log buffer大小，默认是`16M`，最大值是4096M，最小为1M

```sql
show variables like '%innodb_log_buffer_size%';
```

- `重做日志文件（redo log file）`，保存在**硬盘**中，是持久的。

#### 1.4 redo的整体流程

以一个更新事务为例，redo log 流转过程，如下图所示：

![image-20220131220257193](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220131220257193.png)

第1步：先将原始数据从磁盘中读入内存中来，修改数据的内存拷贝（读IO，改内存备份）

第2步：生成一条重做日志并写入redo log buffer，记录的是数据被修改后的值(记录redo log写入缓存)

第3步：当事务commit时，将redo log buffer中的内容刷新到 redo log file，对 redo log file采用追加
写的方式（将缓存写入redo 日志文件）

第4步：定期将内存中修改的数据刷新到磁盘中

> 体会：
>
> write-Ahead log （预先日志持久化）：在持久化一个数据页，先将内存中相应的日志页持久化。

#### 1.5 redo log的刷盘策略

redo log的写入并不是直接写入磁盘的，InnoDB引擎会在写redo log的时候就先写redo log buffer，之后以`一定的频率`刷入到真正的redo log file中。这里的 `一定频率`怎么看待呢？ 这就是我们要说的`刷盘策略`。

刷盘指的是 redo log buffer -> redo log file 的过程，而不是由data buffer -> 磁盘data的过程

![image-20220209192512508](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220209192512508.png)

注意，redo log buffer刷盘到redo log file的过程并不是真正的刷到磁盘中去，只是刷入到`文件系统缓存(page cache)`中去，这是现代操作系统为了提高文件写入效率做的一个优化，真正的写入会交给系统自己来决定（比如page  cache足够大了）。那么对于innodb来说就存在一个问题，如果交给系统来同步，同样如果系统宕机，那么数据还是会丢失。（系统宕机的概率还是比较小的）。

针对这种情况，InnoDB给出`innodb_flush_log_at_trx_commit`参数，该参数控制 commit提交事务时，如何将 redo log buffer 中的日志刷新到 redo log file 中。

它支持三种策略：

- 设置为0：表示每次事务提交时不进行刷盘操作。（系统默认master thread每隔1s进行一次重做日志的同步）
- 设置为1：表示每次事务提交时都将进行同步，刷盘操作==（默认值 ）==
- 设置为2：表示每次事务提交时都只把 redo log buffer 内容写入 page cache，不进行同步。由os自己决定什么时候同步到磁盘文件。

另外，innodb存储引擎有一个后台线程，每个1s，就会把redo log buffer中的内容写到文件系统缓存（page cache），然后调用刷盘操作。

![image-20220209202417497](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220209202417497.png)

也就是说，一个没有提交事务的redo log 记录，也可能会被刷盘。因为在事务执行过程redo log，记录是会写入redo log buffer中，这些redo log 记录会被`后台线程刷盘`。

![image-20220209202826746](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220209202826746.png)

除了后台线程每秒`1次`的轮询操作，还有一种情况，当`redo log buffer`占用空间即将达到`innodb_log_buffer_size`（这个参数默认是16M）的一半的时候，后台线程会主动刷盘。

#### 1.6 不同刷盘策略演示

![image-20220209203259562](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220209203259562.png)

> 小结：innodb_flush_log_at_trx_commit = 1
>
> 为1时，只要事务提交成功，redo log记录就一定在硬盘里，不会有任何数据丢失。
>
> 如果事务执行期间Mysql挂了或宕机，这部分日志丢了，但是事务并没有提交，所以日志丢了也不会又损失。可以保证ACID的D，数据绝对不会丢失，但是这是**效率最差**的。
>
> 建议使用默认值，虽然操作系统宕机的概率理论小于数据库宕机的概率，但是一般用到了事务，那么数据的安全性相对来说更重要一些。

![image-20220209203717674](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220209203717674.png)

> 小结：innodb_flush_log_at_trx_commit = 2
>
> 为2时，只要事务提交成功，redo log buffer 中的内容只写入到文件系统缓存(page cache )。
>
> 如果仅仅是mysql 服务器挂了，不会有任何数据丢失，但是操作系统宕机可能会有1s的数据丢失，这种情况无法满足ACID中的D，但是数值2，肯定是效率最高的。

![image-20220209205254543](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220209205254543.png)

> 小结：innodb_flush_log_at_trx_commit = 0
>
> 为0时，master thread 中每1s进行一次重做日志的fsync操作，因此实例crash最多丢失1s内的事务
>
> （master thread 是负责将缓冲池中的数据异步刷新到磁盘，保证数据的一致性）
>
> 数值0 的话，是一种折中的做法，它的IO效率理论是高于1的，低于2的，这种策略也有丢失数据的风险，无法保证持久性。

#### 1.7 写入redo log buffer过程

1.补充概念：Mini-Transcation

Mysql把对底层页面中的一次原子访问的过程称之为一个`mini-transactiton`，简称`mtr`，比如，向某个索引对应B+树中插入一条记录的过程就是一个`mini-transaction`。一个所谓的`mtr`可以包含一组redo日志，在进行崩溃恢复时这一组redo日志作为一个不可分割的整体。

一个事务可以包含若干条语句，每一条语句其实是由若干个`mtr`组成，每一个mtr又可以包含若干条redo日志，画个图包是它们的关系就是这样:

![image-20220209213858058](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220209213858058.png)



#### 1.8 redo log 小结

**Innodb的更新操作采用的是 write Ahead log(预先日志持久化)策略，即先写日志，再写入磁盘。**

![image-20220209220145641](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220209220145641.png)

## 2、undo 日志

redo log 是事务持久性的保证，undo log 是事务原子性的保证。在事务中`更新数据`的`前置操作`其实要先写入一个`undo log`。

#### 2.1 如何理解undo日志

事务需要保证`原子性`，也就是事务中的操作要么全部完成，要么什么也不做。但有时候事务执行到一半会出现一些情况，

比如：

- 情况一：事务执行过程中可能遇到各种错误，比如`服务器本身的错误`，`操作系统错误`，`甚至是突然断电导致的错误`。
- 情况二：程序员可以在事务执行过程中手动输入 ROLLBACK 语句结束当前事务的执行。

以上情况出现，我们需要把数据改回原先的样子，这个过程称之为 `回滚 `，这样就可以造成一个假象：这个事务看起来什么都没做，所以符合 **原子性** 要求。

- 你`插入一条记录时`，至少要把这条记录的主键值记下来，之后回滚的时候只需要把这个主键值对应的`记录删掉`就好了。(对于每个insert，InnoDB存储引擎会完成一个delete操作)
- 你`删除了一条记录`，至少要把这条记录的内容都记下来，这样之后回滚再把这些内容组成一条新记录`插入到表`中就好了。（对于每个delete，InnodB存储引擎会执行一个insert操作）
- 你`修改了一条记录`。至少要把修改过这条记录前的旧值都记录下来，这样之后回滚时再把这条记录`更新为旧值`就好了。（对于每个Update，Innodb存储引擎会执行一个相反的update，将之前修改的行放回去）

Mysql把这些为了回滚而记录的这些内容称之为`撤销日志或者叫回滚日志`（undo log）

注意：由于**查询操作（select）**并不会修改任何用户记录，所以在查询操作执行时，并`不需要`记录对应的undo log日志。

#### 2.2 undo日志的作用

- 作用1：回滚数据

用户对undo日志可能存在`误解`：undo用于将数据库物理的恢复到执行语句或事务之前的样子。

但是事实并非如此。undo是`逻辑日志`，因此只是将数据库逻辑地恢复到原来的样子。所有的逻辑修改都被取消了，但是数据结构和页本身在回滚之后可能大不相同。

这是因为在多用户并发系统中，可能会有十、数百、数千个并发事务。数据库的主要任务就是协调对数据记录的访问。比如：一个事务在修改当前页的某几条记录，同时还会有别的事务在对同一个页中另几条记录进行修改。因此，不能将一个页回滚到事务开始的样子，因为这样会影响其他事务正在进行的工作。

- 作用2：MVCC

undo的另外一个作用是MVCC，即在InnoDB存储引擎中MVCC的实现是通过undo来完成。当用户读取一行记录时，若该记录已经被其他事务占用，当前事务可以通过undo读取之前的行版本信息，以此来实现非锁定读。

#### 2.3 undo的存储结构

**1、回滚段与undo页**

InnoDB对undo log的管理采用段的方式，也就是`回滚段(rollback segment)`。每个回滚段记录了`1024`个`undo log segment`，而在每个undo log segment段中进行`undo页`的申请。

- 在 InnoDB1.1版本之前 （不包括1.1版本），只有一个rollback segment，因此支持同时在线的事务限制为 1024 。虽然对绝大多数的应用来说都已经够用。
- 从1.1版本开始InnoDB支持最大 128个rollback segment ，故其支持同时在线的事务限制提高到了 128*1024 。

```sql
show variables like 'innodb_undo_logs';
```

**2、回滚段与事务**

1. 每个事务只会使用一个回滚段，一个回滚段在同一时刻可能服务于多个事务。
2. 当一个事务开始的时候，会制定一个回滚段，在事务进行的过程中，当数据被修改时，原始的数据会被复制到回滚段中。
3. 在回滚段中，事务会不断填充盘区，直到事务结束或者所有的空间被用完。如果当前的盘区不够用，事务会在段中请求扩展下一个盘区，如果所有分配的盘区都被用完，事务会覆盖最初的盘区或者在回滚段允许的情况下扩展新的盘区来使用。
4. 回滚段存在于undo表空间中，在数据库中可以存在多个undo表空间，但同一时刻只能使用一个undo表空间。
5. 当事务提交时，InnoDB存储引擎会做以下两件事：
   - 将undo log放入列表中，以供之后的purge操作
   - 判断undo 所在的页是否可以重用，若可以分配给下一个事务使用

**3、回滚段中的数据分类**

- ==未提交的回滚数据(uncommitted undo information)==
- ==已经提交但未过期的回滚数据(committed undo information)==
- ==事务已经提交并过期的数据(expired undo information)==

#### 2.4 undo的类型

在InnoDB中存储引擎中，undo log分为：

- insert undo log
- update undo log

#### 2.5 undo log的生命周期

**1.** **简要生成过程**

**只有Buffer Pool的流程：**

![image-20220211143013821](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220211143013821.png)

==有了redo log 和undo log后==

![image-20220211143308423](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220211143308423.png)

**2.** **详细生成过程**

![image-20220211143457991](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220211143457991.png)

当我们执行insert的时候：

```sql
begin;
INSERT INTO user (name) VALUES ("tom");
```

![image-20220211144332368](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220211144332368.png)

当我们执行update时：

![image-20220211144413767](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220211144413767.png)

```sql
UPDATE user SET id=2 WHERE id=1;
```

![image-20220211144436006](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220211144436006.png)

**3.undo log是如何回滚得**

以上面的例子来说，假设执行rollback，那么对应的流程应该是这样：

1、通过undo no=3的日志把id=2的数据删除

2、通过undo no=2的日志把id=1的数据的deletemark还原成0

3、通过undo no=1的日志把id=1的数据的name还原成Tom

4、通过undo no=0的日志把id=1的数据删除

**4.undo log的删除**

- 针对于insert undo log

因为insert操作的记录，只对事务本身可见，对其他事务不可见。故该undo log可以在事务提交后直接删除，不需要进行purge操作。

- 针对于update undo log

该undo log可能需要提供mvcc机制，因此不能在提交事务时就进行删除。提交时放入undo log链表，等待purge线程进行最后的删除。

#### 2.6 小结

![image-20220210094627994](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220210094627994.png)

==undo log 是逻辑日志，对事务回滚时，只是将数据库逻辑恢复到原来的样子==

==redo log 是物理日志，记录的是数据页的物理变化，undo log 不是 redo log的逆过程。==

