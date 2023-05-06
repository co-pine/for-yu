### 快速开始
1. 复制自己的 cookie 和 请求参数（手动给请求参数的数字值加上引号）
2. 在 BaiDuUtil 填上百度智能云的 APIKey 和 APISecret


### 主体思路
1. 进入网站，通过开发者工具找到获取文章的接口

2. 观察接口，特别是认证的字段（cookie、referer等）。在接口测试工具（我这里用的是 postman ）调试并拿到数据

3. 预置一套包含了自己文章各个领域的分层级标签 json 串，并将该 json 串的所有 key 作为预置标签。参考如下（由 chatGPT 生成 ）：

   ```json
   {
     "编程语言": [
       {
         "C语言": [
           "C基础语法",
           "指针",
           "内存管理",
           "C标准库"
         ]
       },
       {
         "C++": [
           "C++基础语法",
           "面向对象编程",
           "模板",
           "标准库"
         ]
       },
       {
         "Java": [
           {
             "Java基础": [
               "Java SE",
               "Java EE",
               "Java SE Embedded",
               "Java ME",
               "Java Card",
               "JavaFX"
             ]
           },
           {
             "Java框架": [
               "Spring Framework",
               "Spring Boot",
               "Hibernate",
               "MyBatis",
               "Spring Security",
               "Struts",
               "JSF",
               "Play Framework",
               "Vert.x"
             ]
           },
           {
             "Java工具": [
               "Maven",
               "Gradle",
               "Ant",
               "Jenkins",
               "SonarQube",
               "JUnit"
             ]
           },
           {
             "Java虚拟机": [
               "JVM",
               "JRE",
               "JIT编译器",
               "垃圾回收器（GC）"
             ]
           },
           {
             "Java应用": [
               "Android应用开发",
               "Scala",
               "Kotlin",
               "Clojure"
             ]
           }
         ]
       },
       {
         "Python": [
           "Python基础语法",
           "面向对象编程",
           "函数式编程",
           "Python标准库",
           {
             "Python框架": [
               "Django",
               "Flask",
               "Pyramid",
               "Tornado"
             ]
           },
           {
             "Python科学计算": [
               "NumPy",
               "Pandas",
               "SciPy",
               "Matplotlib"
             ]
           }
         ]
       },
       {
         "JavaScript": [
           "JavaScript基础语法",
           "DOM操作",
           "jQuery",
           "React",
           "Vue",
           "Angular"
         ]
       },
       {
         "Go": [
           "Go基础语法",
           "并发编程",
           "Web应用开发",
           "Go标准库"
         ]
       },
       {
         "Ruby": [
           "Ruby基础语法",
           "面向对象编程",
           "Ruby标准库",
           {
             "Ruby框架": [
               "Ruby on Rails",
               "Sinatra",
               "Padrino"
             ]
           }
         ]
       }
     ],
     "数据库": [
       "MySQL",
       "Oracle",
       "SQL Server",
       "PostgreSQL",
       "MongoDB",
       "Redis"
     ],
     "网络编程": [
       "TCP/IP",
       "HTTP/HTTPS",
       "WebSocket",
       "RPC"
     ],
     "操作系统": [
       "Linux",
       "Windows",
       "macOS"
     ],
     "设计模式": [
       "单例模式",
       "工厂模式",
       "装饰器模式",
       "观察者模式"
     ]
   }
   ```

4. 根据获取到的文章数据提取文章的标签，这里我用了两种方法：

   - 用百度 nlp 领域的文章标签 API （https://cloud.baidu.com/doc/NLP/s/7k6z52ggx，首次注册会送 50万 调用次数）。这个 API 会根据输入的文章内容和标题返回多个标签和相应的分数，然后拿着标签去已经预置的 json 串里比对，即可得到该文章的所有分层级标签。比如，用标题为 “SpringSecurity 学习笔记——自定义登录流程（SpringSecurity + JWT + Redis）”的文章通过 API 生成的标签为 [{"score":0.8367354273796082,"tag":"Redis"},{"score":0.8367354273796082,"tag":"Spring Boot"},{"score":0.8367354273796082,"tag":"Spring Security"},{"score":0.8367354273796082,"tag":"token"}]，把 tag 对应的内容拿出来去 json 串里比较，最终得到的结果如下：

   ```json
   ["Spring Boot", "Spring Security","Redis"]
   ```

   ​	而 token 在我们的预置标签里并没有，所以被丢弃，对应的分层级标签如下：

   ```json
   {
       "标签": [
           {"编程语言": [
               {
                   "Java": [
                       {
                           "Java框架": ["Spring Boot", "Spring Security"]
                       }
                   ]
               }
           ]},
           {"数据库": ["Redis"]}
       ]
   }
   ```

   ​       其实以上结果是理想情况，根据我的测试，实际情况是这个 API 返回的标签大部分时候并不精准甚至有时候没有返回值

   - 循环所有标签，判断文章内容、标题是否包含该标签内容，如果包含则将其作为该文章的标签之一。

   虽然 API 的方法不太精准，但我还是选择了这个方法，因为第二种方法偶然性太大了。比如我在讲述 Redis 的文章里提了一嘴 MongoDB ，就会导致 MongoDB 也是该文章的标签，但这并不是我想要的结果。也想过在第二种方法的基础上改进，比如出现多次才符合、标题和内容占据不同比重等等，但这其中具体几次、具体占多少比重很难找到一个合理的数据导致效果不会有太大提升，因此我最终放弃了这个方案

5. 生成的标签在于 json 串里的标签比对之前，还需要进行简单的清洗。比如生成的标签里是 SpringBoot （没有空格），而实际我们预置的标签是 Spring Boot

6. 做完这些，就拿到了文章内容、标题及其标签，进一步可以转换成 key 为 label，value 为文章信息的 map （代码实际做到这一步），方便后续展示。接下来就是解析到自己的网站进行展示的问题。我这里没有自己的网站（平时写博客都是在星球和各大平台），就说下思路。其实这里的需求很像皮总通过 chatGPT 做的编程百科网站，让 chatGPT 给出分类（也就是上面我们预置的 json 串），根据 json 串的嵌套关系生成多级路由导航，考虑到层级太深也可以进行适当删减。而文章内容及其对应标签在上一步我们已经整理出来了，完全可以通过 VuePress 快速搭建起一个网站。

tips：代码实际做到了拿到数据、标题及其标签并转换成 key 为 label，value 为文章信息的 map ，展示部分没有实践进行。
