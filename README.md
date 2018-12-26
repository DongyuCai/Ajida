#### 注意事项：

##### 服务器的tomcat安装路径下（与webapps同级），新建一个webapps_backup文件夹，用于备份war包，ajida会自动将原war包备份到这里。

##### 如何使用：

###### 在项目的pom.xml中添加ajida依赖
 
###### 新建一个类，使用Ajida.update方法，参考如下示例，即可将项目跟新到指定环境。
 
###### 执行更新前，还是需要本地maven clean package打包一下项目，否则将会以上次打包结果进行跟新。
```java
  //配置文件路径，ajiada会自动将resourceDir下的配置文件，全部复制到targetDir下
  String resourceDir = "D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\config\\test";
  String targetDir = "D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\target";
  String project = "\\xjp-user";//工程名，必须要按照例子写，前面带\\
  //远程账号
  String linuxIp = "192.168.199.45";
  String linuxUsername = "root";
  String linuxPassword = "ybsl1234";
  String tomcatDir = "/usr/local/apache-tomcat-9.0.12";//服务器上tomcat安装目录

  Ajida.update(resourceDir, targetDir, project, linuxIp, linuxUsername, linuxPassword, tomcatDir, restartTomcat);
```
