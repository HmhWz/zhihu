# zhihu
仿照知乎做的一个Java web项目，是一个sns+资讯的web应用。使用SpringBoot+Mybatis+velocity开发。数据库使用了redis和mysql，同时加入了异步消息等进阶功能，同时使用python爬虫进行数据填充。

   
## 基本框架开发
    
    controller中使用注解配置，requestmapping，responsebody基本可以解决请求转发以及响应内容的渲染。responsebody自动选择viewresolver进行解析。

    使用velocity编写页面模板，注意其中的语法使用。常用$!{}和${}
    
    使用http规范下的httpservletrequest和httpservletresponse来封装请求和相响应，使用封装好的session和cookie对象。
    
    使用重定向的redirectview和统一异常处理器exceptionhandler

    IOC解决对象实例化以及依赖传递问题，解耦。
    
    AOP解决纵向切面问题，主要实现日志和权限控制功能。
    
    aspect实现切面，并且使用logger来记录日志，用该切面的切面方法来监听controller。

   
## 用户注册登录以及使用token

	完成用户注册和登录的controller,service和dao层代码

	新建数据表login_ticket用来存储ticket字段。该字段在用户登录成功时被生成并存入数据库，并被设置为cookie，
	下次用户登录时会带上这个ticket，ticket是随机的uuid，有过期时间以及有效状态。

	使用拦截器interceptor来拦截所有用户请求，判断请求中是否有有有效的ticket，如果有的话则将用户信息写入Threadlocal。
	所有线程的threadlocal都被存在一个叫做hostholder的实例中，根据该实例就可以在全局任意位置获取用户的信息。

	该ticket的功能类似session，也是通过cookie写回浏览器，浏览器请求时再通过cookie传递，区别是该字段是存在数据库中的，并且可以用于移动端。

	通过用户访问权限拦截器来拦截用户的越界访问，比如用户没有管理员权限就不能访问管理员页面。

	配置了用户的webconfiguration来设置启动时的配置，这里可以将上述的两个拦截器加到启动项里。

	配置了json工具类以及md5工具类，并且使用Java自带的盐生成api将用户密码加密为密文。保证密码安全。

	数据安全性的保障手段：https使用公钥加密私钥解密，比如支付宝的密码加密，单点登录验证，验证码机制等。

	ajax异步加载数据 json数据传输等。

## 新增发表问题功能，并防止xss注入以及敏感词过滤

	新增Question相关的model，dao，service和controller。

	发布问题时检查标题和内容，防止xss注入，并且过滤敏感词。

	防止xss注入直接使用HTMLutils的方法即可实现。

	过滤敏感词首先需要建立一个字典树，并且读取一份保存敏感词的文本文件，然后初始化字典树。
	最后将过滤器作为一个服务，让需要过滤敏感词的服务进行调用即可。

## 新增评论和站内信功能

	首先建立表comment和message分别代表评论和站内信。

	依次开发model，dao，service和controller。

	评论的逻辑是每一个问题下面都有评论，显示评论数量，具体内容，评论人等信息。

	消息的逻辑是，两个用户之间发送一条消息，有一个唯一的会话id，这个会话里可以有多条这两个用户的交互信息。
	通过一个用户id获取该用户的会话列表，再根据会话id再获取具体的会话内的多条消息。

	逻辑清楚之后，再加上一些附加功能，比如显示未读消息数量，根据时间顺序排列会话和消息。

	本节内容基本就是业务逻辑的开发，没有新增什么技术点，主要是前后端交互的逻辑比较复杂，前端的开发量也比较大。

## 新增点赞和点踩功能，使用Redis实现

	开发点踩和点赞功能，在此之前根据业务封装好jedis的增删改查操作，放在util包中

	根据需求确定key字段，格式是 like：实体类型：实体id 和 dislike：实体类型：实体id 这样可以将喜欢一条新闻的人存在一个集合，不喜欢的存在另一个集合。通过统计数量可以获得点赞和点踩数。

	一般点赞点踩操作是先修改redis的值并获取返回值，然后再异步修改mysql数据库的likecount数值。这样既可以保证点赞操作快速完成，也可保证数据一致性。

	本次开发过程中遇到了请求超时的问题，经过排查之后是漏写了某个接口的服务，导致前端获取不到后端需要传的数据，而前端代码会不断检测这个数据的值以完成后续操作，导致页面无法完成解析。后来回滚到上一个版本后才发现bug所在并解决了该问题。

## 新增异步消息功能 新增邮件发送组件

	在之前的功能中有一些不需要实时执行的操作或者任务，我们可以把它们改造成异步消息来进行发送。

	具体操作就是使用redis来实现异步消息队列。代码中我们使用事件event来包装一个事件，事件需要记录事件实体的各种信息。

	我们在async包里开发异步工具类，事件生产者，事件消费者，并且开发一个eventhandler接口，让各种事件的实现类来实现这个接口。

	事件生产者一般作为一个服务，由业务代码进行调用产生一个事件。而事件消费者我们在代码里使用了单线程循环获取队列里的事件，并且寻找对应的handler进行处理。

	如此一来，整个异步事件的框架就开发完成了。后面新加入的登录，点赞等事件都可以这么实现。

	新增邮件功能，主要是引入mail依赖，并且配置好自己的邮箱信息，以及邮件模板，同时在业务代码中加入发邮件的逻辑即可。

## 新增关注功能，开发关注页面和粉丝页面

	新增关注功能，使用redis实现每一个关注对象的粉丝列表以及每一个用户的关注对象列表。
	通过该列表的crud操作可以对应获取粉丝列表和关注列表，并且实现关注和取关功能。

	由于关注成功和添加粉丝成功时同一个事务里的两个操作，可以使用redis的事务multi来包装事务并进行提交。

	除此之外，关注成功或者被关注还可以通过事件机制来生成发送邮件的事件，由异步的队列处理器来完成事件响应，同样是根据redis来实现。

	对于粉丝列表，除了显示粉丝的基本信息之外，还要显示当前用户是否关注了这个粉丝，以便前端显示。

	对于关注列表来说，如果被关注对象是用户的话，除了显示用户的基本信息之外，还要显示当前用户是被这个用户关注，以便前端显示。


## 使用solr搭建全文搜索引擎，开发知乎的全文搜索功能

	solr默认英文分词，需要加入中文分词工具IK-Analyzer
	
	solr中一个core代表一个全文搜索集，我们可以在server文件夹中找到我们创建的
	core。然后根据需要修改conf里的配置文件，首先修改managed-schema来设置分词规则，我们在此加入中文分词类型，并且配置其索引分词和查询分词，此处需要引入IK-Analyzer的jar包，jar包可以通过maven项目打包而获得。

	索引分词指的是建立索引使用的分词，比如你好北京，可以分为你 你好 北京 北 等情况。
	而查询分词是根据需求进行查询时的分词，可以分为你好 北京。

	为了通过数据库向solr导入数据，我们需要配置数据导入处理器，这是需要修改solrconfig文件来配置数据导入处理器，并且在solr-data-config中配置本地数据库地址，这样就可以在solr的web页面中进行数据库导入了。导入之后自动建立索引，我们就可以使用solr来对数据库进行全文搜索了。比如mysql数据库进行普通搜索，把数据导入solr进行全文搜索。

	开发搜索相关功能，开发service并且接入solr的api，从而连接本机的solr服务并且执行查询和索引操作。
	只需要指定关键字，以及我们要搜索的栏目（本例中主要title和content，所以传入这两个字段，并且在搜索结果中加亮关键字。
	开发相关controller以及页面。并且在新增问题以后执行异步事件，将新增的问题数据加入solr的数据库中，以便后面进行搜索。


## 单元测试与压力测试，项目打包及部署等收尾工作

	单元测试保证模块的可用性，每个模块测试完以后再进行集成测试，maven打包时会自动执行单元测试。
	SpringBoot中只需在test类中做好配置便可以进行spring相关的单元测试。

	使用压测工具apache2-utils， LoadRunner等进行压力测试，更好地了解系统性能

	centos上支持Apachebench压测工具，可以并发发送大量http请求来完成压力测试，可以看出机器的负载状况。

	在虚拟机上安装jdk8,tomcat8,redis,maven,nginx,mysql等基本环境。
	tomcat默认监听127.0.0.1的8080端口，只允许本地访问，这样保障安全。
	所以可以在外网访问时加入一层Nginx进行反向代理和负载均衡，让域名或ip访问首先找到Nginx，再由nginx找到tomcat。




