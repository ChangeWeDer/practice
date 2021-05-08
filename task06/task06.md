## 一、实现原理
类似对称加密技术，在Nginx端设置好密钥，然后在服务端使用相同的密钥进行md5加密，生成资源链接后，再通过该链接进行访问Nginx，Nginx对链接信息进行校验通过后方可通行。

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e35b6662339a4f6085de569ffb117192~tplv-k3u1fbpfcp-watermark.image)

## 二、Nginx配置
1.进入到Nginx的安装目录

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a2a00b9f41254f03a984cfd379dc4846~tplv-k3u1fbpfcp-watermark.image)

2.输入命令`./configure --with-http_secure_link_module`，安装secure_link模块，接着执行`make`、`make install`

3.执行完毕后，`nginx -V`查看是否成功安装

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/1221b4e491db4289a0a21c99dc7f137c~tplv-k3u1fbpfcp-watermark.image)

4.进入到nginx的配置文件nginx.conf，进行如下配置（主要是添加了secure_link、secure_link_md5），其中secret_key为密钥，可自行修改，必须保证服务端的密钥与nginx的密钥相同即可.可自行在location后添加拦截规则

```js
 server {
        listen       80;
        server_name  localhost;

        location / {
        secure_link $arg_md5,$arg_expires;  #这里配置了2个参数一个是arg_md5，一个是arg_expires
        secure_link_md5 "secret_key$secure_link_expires$uri"; #secret_key为自定义的加密串   
    if ($secure_link = "") {
        return 403;       #资源不存在或哈希比对失败
        }
    if ($secure_link = "0") {
        return 403;      #时间戳过期 
        }
        }
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
```
**secure_link**

由校验值和过期时间组成，其中校验值将会与 secure_link_md5中的指定参数的MD5哈希值进行对比。

如果两个值不一致，变量的值是空；如果两个值一致，则进行过期检查；如果过期了，则变量值是0；如果没过期，则为1。

如果链接是有时效性的，那么过期时间用时间戳进行设置，在MD5哈希值后面声明。如果没有设置过期时间，该链接永久有效。

**secure_link_md5**

该md5值将会和url中传递的md5值进行对比校验（所以需要保证值得顺序一致）

## 二、Java代码配置

```java
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

    public class TEST {
    public static void main(String[] args) {
        String time = String.valueOf(System.currentTimeMillis() / 1000 + 600);// +600代表600秒后地址失效
        String md5 = Base64.encodeBase64URLSafeString(DigestUtils.md5("secret_key"+time+"/test.mp4"));
        System.out.println("http://192.168.1.9/test.mp4?md5=" + md5 + "&expires=" + time);
}
}
```

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3360c9e56fba4171beb8d62cd2be01c0~tplv-k3u1fbpfcp-watermark.image)

注意：

Java中得md5与nginx中的secure_link_md5是相互对应的，顺序不能写错，出现不对应的问题。

