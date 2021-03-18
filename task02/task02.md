# Linux Socket使用多线程实现两个客户端之间的echo（阿里云服务端转发数据，1个服务器，2个客户端）

## 任务描述
客户端A，B均运行Ubuntu。
云端服务器C，运行Ubuntu。

A，B各自与C建立TCP连接。

A发送TCP包给C，C转发至B。
B收到后原封发回C，C转发至A。
A将所用时间显示出来。

**要求：**
A，B，C端全部程序使用C/C++编写，可使用外部库，但仅限于C/C++编写的库。

详细实现blog：
https://www.upstudy.top/index.php/archives/51/