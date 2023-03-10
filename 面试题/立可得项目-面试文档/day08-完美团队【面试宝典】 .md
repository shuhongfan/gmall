# 第8章 完美团队【面试宝典】

## 1.你知道有哪几种数据同步的方式？

（1）使用MQ方式在代码中实现数据同步。

（2）采用定时任务，进行数据同步。

（3）采用阿里的数据同步中间件 canal实现数据同步，原理是通过监听binlog日志来实现。

（4）使用Logstash实现数据同步。

## 2.在项目中为什么使用logstash做数据同步？

使用logstash，不需要写任何代码，不用对工程有任何侵入。只需要编写pipline脚本即可。

## 3.logstash如何做数据同步？

（1）环境搭建：使用docker拉取logstash镜像，版本要和elasticsearch版本相同

（2）上传mysql8的jdbc驱动包。

（3）创建容器，挂载mysql8的jdbc驱动包到容器。

（4）编写pipline脚本。输入：mysql , 输出es。 

（5）启动容器，观察数据是否自动同步。

## 4.项目中有没有用到ES，应用场景是什么？如何使用的？

项目中使用ES存储订单数据。采用logstash进行数据同步。小程序的订单查询、管理后台的订单查询、智能排货、区域销售排行统计等功能都用到es查询以及聚合运算。

## 5.项目中要求对订单数据要进行统计，但是订单表记录数很大，每次统计需要大量时间，如何优化？

我们可以采用定时任务，每天凌晨后对前一天数据进行汇总统计，并存入订单汇总表中，这样统计只需要对订单汇总表进行聚合运算即可。

## 6.项目中的曲线图、折线图、柱状图在后端如何实现？

曲线图、折线图、柱状图通常是表示趋势的图，其结构是一样的。我们需要封装一个VO类，有两个属性。第1个是List<String> ，代表X轴。第2个是  List<Integer> 表示数据。  我们在对数据库进行统计后，需要加工成Vo类返回给前端。

## 7.说出git分支的作用，以及在项目中的使用方法。

为什么要创建分支?

​		比如我们开发完了一个app上线了,接下那就是迭代功能开发了,如果上线的app出现了一个严重的bug,要你放下手头新功能的开发去解决这个bug,然后在发布一个新版本,如果你要是就在你要迭代功能的项目上进行修改发布的话,那肯定是不行的,且先不谈有没有新的bug出现,时间是也是不允许的,发布的前提还要把新功能完善好才行,要是删掉新功能的代码也不怎么现实,要是业务逻辑少一点还好说,要是多的话那还真是有点无从下手了,所以git的分支就很好的解决了这个问题。

如何使用？

​		主分支（默认创建的Master分支）只用来分布重大版本（对于每个版本可以创建不同的标签，以便于查找）；日常开发应该在另一条分支上完成，可以取名为Develop；临时性分支，用完后最好删除，以免分支混乱。多人开发时，每个人还可以分出一个自己专属的分支，当阶段性工作完成后应该合并到上级分支。

## 8.介绍做项目时遇到的难点？

我当时在统计分析的功能时，对订单表进行各种聚合运算。在开发环境和测试环境都没有遇到问题。但是在生产环境跑了一段时间，发现接口运行越来越慢，经过分析发现原来是订单表记录过多造成的问题。我们开发小组经过讨论，决定采用定时任务汇总的方案来解决。具体的方式是，编写定时任务逻辑，统计前一天的数据，存入到一张订单汇总表中，而统计分析的功能则直接对订单汇总表进行聚合，这样极大提升了接口的响应速度。

## 9.项目中开发接口遇到问题如何调试？

在前后端分离开发模式下，我们要首先通过接口测试来验证我们编写的代码是否正常。一般临时性的测试可以使用postman，更好的方式是使用vscode的restClient插件来进行测试，这样只需要第一次编写好测试脚本，之后测试比postman要方便的多。

当接口测试没有问题后，需要进行前后端联调。前后端联调中如果遇到问题，首先根据http状态码判断无法联调成功的原因，404表示地址不对，500表示后端报错。如果404就要检查是前端和后端，哪一个没有按照接口文档编写地址导致无法对接成功。如果500，就要查看后端控制台的报错信息，根据异常信息找到报错的语句。这种情况大多数是因为参数传递不正确。

根据上述方式，基本可以断定是前端的问题还是后端的问题。如果是后端的问题，根据报错信息无法找到原因的，可以再尝试在关键代码上打上断点，逐条运行，观察变量的内容是否和预期结果一致 。



## 10.如何通过AOP对某些方法进行统一操作?

Aop常用注解

@Aspect:作用是把当前类标识为一个切面供容器读取
@Pointcut：Pointcut是植入Advice的触发条件。每个Pointcut的定义包括2部分，一是表达式，二是方法签名。方法签名必须是 public及void型。可以将Pointcut中的方法看作是一个被Advice引用的助记符，因为表达式不直观，因此我们可以通过方法签名的方式为 此表达式命名。因此Pointcut中的方法只需要方法签名，而不需要在方法体内编写实际代码。
@Around：环绕增强
@AfterReturning：后置增强
@Before：标识一个前置增强方法
@AfterThrowing：异常抛出增强
@After: final增强，不管是抛出异常或者正常退出都会执行

具体操作步骤：

（1）新建注解类AopFilter  ，定义属性

（2）在某操作类的方法上使用AopFilter标注 

（3）新建切面类 ，在切面类的方法中通过获取AopFilter注解中的属性来处理逻辑。
