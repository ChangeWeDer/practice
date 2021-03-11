使用Ubuntu 18 上传本地文件到百度云盘中

https://www.upstudy.top/index.php/archives/50/



# 一、环境准备
1.python2.7及以上，如果有则跳过这步。

```shell
sudo apt install python2.7
```
2.安装相关库

```shell
sudo apt install python-pip   #安装pip

sudo pip install requests  #安装requests

sudo -H pip install bypy  #安装bypy，我这里根据执行sudo pip install bypy时的waring加上了-H安装到系统库里，如果不管，会导致bypy安装到了用户的库，无法直接使用。
```

# 二.使用bypy库

```shell
bypy info  #第一次使用需要进行认证
```
执行命令后，会返回一个可访问的网址，并且让你输入验证码。


直接使用浏览器打开，复制这段验证码，然后粘贴下去执行，然后等待几分钟



# 三、上传文件

```shell
bypy upload 文件名
```

打开百度网盘，会发现文件存在 我的网盘/我的应用数据/bypy 路径下
