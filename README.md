# auth
springboot 打包
点击左下角maven
右上角父项目[生命周期]-> package
找到jar文件，上传到服务器


查看端口占用情况
lsof -i:8088
或
netstat -tunlp | grep 端口号

杀死pid;
kill -9 PID

后台运行jar,输出日志：
nohup java -jar app-0.0.1-SNAPSHOT.jar > log.txt  2>&1 &








Mybatis | Mybatis-plus配置多数据源，连接多数据库
https://blog.csdn.net/weixin_45866737/article/details/122463180


.gitignore： 用git做版本控制时，忽略文件配置（不用版本管理可删除 没影响）。
HELP.md md： 帮助文档（可删除 没影响）。
mvnw： linux上处理maven版本兼容问题的脚本（可删除 没影响）。
mvnw.cmd： windows上处理maven版本兼容问题的脚本（可删除 没影响）。
xxx.iml： 是IDEA特有的文件。每个IDEA的项目都会生成一个与项目同名的 .iml文件 用于保存这个项目的配置 （删了程序重新导入后还会生成 但由于配置丢失可能会造成程序异常。
————————————————
版权声明：本文为CSDN博主「雨汨」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/qq_33761723/article/details/123056695