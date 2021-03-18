#include <sys/types.h>
#include <sys/socket.h>
#include <stdio.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <errno.h>
#include <unistd.h>
#include <ctype.h>
#include <string.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/time.h>//计算时间
#define PORT  8888//端口
#define IP  "120.79.165.96"//服务端IP地址
#define BUFFERSIZE 1024
typedef unsigned char BYTE;
pthread_t receiveID;

//字符格式转换
void tolowerString(char *s)
{
    int i=0;
    while(i < strlen(s))
    {
        s[i] = tolower(s[i]);
        ++i;
    }
}

//接收服务端发过来的数据
void receive(void *argv)
{
    int sockclient = *(int*)(argv);
    BYTE recvbuff[BUFFERSIZE];
    while(recv(sockclient, recvbuff, sizeof(recvbuff), 0)!=-1) //receive
    {

        BYTE msg[BUFFERSIZE];
        int dest = 0;//存储的是在服务端发送方的id,在这里不需要使用
        sscanf(recvbuff, "%d%s",&dest,msg);

        fprintf(stdout, "接收到信息\n发送端索引：%d 消息为：%s\n", dest, msg);
        fprintf(stdout, "消息回响\n\n");
        //返回回响数据
        send(sockclient, recvbuff, strlen(recvbuff)+1, 0);
    }
}
int main()
{
    //新建socket
    int sockclient = socket(AF_INET,SOCK_STREAM, 0);
    //地址
    struct sockaddr_in servaddr;
    memset(&servaddr, 0, sizeof(servaddr));
    //设置初始值
    servaddr.sin_family = AF_INET;
    servaddr.sin_port = htons(PORT);  ///server port
    servaddr.sin_addr.s_addr = inet_addr(IP);  //server ip

    //连接服务端
    if (connect(sockclient, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0)
    {
        fprintf(stderr,"连接服务器失败");
    }
    fprintf(stdout, "连接服务器成功\n");
    //添加一个接收数据的线程
    pthread_create(&receiveID, NULL, (void *)(receive), (void *)(&sockclient));

    BYTE buff[BUFFERSIZE];

    while (fgets(buff, sizeof(buff), stdin) != NULL)
    {
        tolowerString(buff);
        //主线程发送数据
        if(send(sockclient, buff, strlen(buff)+1, 0) == -1) //send
        {
            fprintf(stderr, "发送失败\n");
            continue;
        }
        memset(buff, 0, sizeof(buff));
    }
    close(sockclient);
    return 0;
}

