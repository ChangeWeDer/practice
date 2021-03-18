#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/time.h>
#include <stdio.h>
#include <errno.h>
#include <ctype.h>
#include <string.h>
#include <pthread.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#define PORT  8888
#define BACKLOG 10
#define MAXCONN 100
#define BUFFSIZE 1024
typedef unsigned char BYTE;
//客户端信息
typedef struct ClientInfo
{
    struct sockaddr_in addr;
    int clientfd;
    int isConn;
    int index;
} ClientInfo;
pthread_mutex_t activeConnMutex;
pthread_mutex_t clientsMutex[MAXCONN];
pthread_t threadID[MAXCONN];
ClientInfo clients[MAXCONN];

//转换为小写
void tolowerString(char *s)
{
    int i=0;
    while(i < strlen(s))
    {
        s[i] = tolower(s[i]);
        ++i;
    }
}

//新建一个线程，管理各自的客户端
void clientManager(void* argv)
{
    ClientInfo *client = (ClientInfo *)(argv);

    BYTE buff[BUFFSIZE];
    int recvbytes;

    int i=0;
    int clientfd = client->clientfd;
    struct sockaddr_in addr = client->addr;
    int isConn = client->isConn;
    int clientIndex = client->index;

    //接收数据
    while((recvbytes = recv(clientfd, buff, BUFFSIZE, 0)) != -1)
    {
        tolowerString(buff);    //格式转换

        char msg[BUFFSIZE]; //客户端消息主体
        int dest = clientIndex; //消息发送的目标客户端

        sscanf(buff, "%d%s", &dest, msg);

        fprintf(stdout, "发送端索引：%d 接收端索引：%d  ，消息为：%s\n",clientIndex, dest, msg);

        //加上对目标客户端互斥锁
        pthread_mutex_lock(&clientsMutex[dest]);

        //printf("发送端索引：%d 接收端索引：%d\n",clientIndex,dest);
        char str[10];
        sprintf(str,"%d ",clientIndex);
        strcat(str,msg);
        //给目标客户端发送信息
        if(send(clients[dest].clientfd, str, strlen(str)+1, 0) == -1)
        {
            fprintf(stderr, "消息发送失败\n");
            pthread_mutex_unlock(&clientsMutex[dest]);
            break;
        }
        printf("消息发送成功\n");
        //释放锁
        pthread_mutex_unlock(&clientsMutex[dest]);


    }   //end while

}

int main()
{
    int i=0;
    for(;i<MAXCONN;++i)
        //初始化客户端数组互斥锁
        pthread_mutex_init(&clientsMutex[i], NULL);

    for(i=0;i<MAXCONN;++i)
        //初始化客户端连接
        clients[i].isConn = 0;

    int listenfd;
    struct sockaddr_in  servaddr;

    //新建一个socket
    if((listenfd = socket(AF_INET, SOCK_STREAM, 0)) == -1)
    {
        fprintf(stdout, "创建socket失败\n");
        exit(0);
    }
    else
        fprintf(stdout, "创建socket成功\n");

    fcntl(listenfd, F_SETFL, O_NONBLOCK);       //设置socket不阻塞 F_SETFL：设置文件状态标记

    //设置server地址
    memset(&servaddr, 0, sizeof(servaddr));  //初始化服务地址
    servaddr.sin_family = AF_INET;           //AF_INET 使用TCP协议
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);    //任何输入地址
    servaddr.sin_port = htons(PORT);            //设置端口

    //绑定socket
    if(bind(listenfd, (struct sockaddr*)(&servaddr), sizeof(servaddr)) == -1)
    {
        fprintf(stdout, "绑定socket失败\n");
        exit(0);
    }
    else
        fprintf(stdout, "绑定socket成功\n");

    //listen状态
    if(listen(listenfd, BACKLOG) == -1)
    {
        fprintf(stdout, "监听socket失败\n");
        exit(0);
    }
    else
        fprintf(stdout, "监听socket成功\n");


    while(1)
    {
        //为新连接找一个空位置
        int i=0;
        while(i<MAXCONN)
        {
            //上锁
            pthread_mutex_lock(&clientsMutex[i]);
            if(!clients[i].isConn)
            {
                pthread_mutex_unlock(&clientsMutex[i]);
                break;
            }
            //释放锁
            pthread_mutex_unlock(&clientsMutex[i]);
            ++i;
        }
        //连接已满，初始化第一个连接
        if (i == MAXCONN) i = 0;

        //accept状态
        struct sockaddr_in addr;
        int clientfd;
        int sin_size = sizeof(struct sockaddr_in);
        if((clientfd = accept(listenfd, (struct sockaddr*)(&addr), &sin_size)) == -1)
        {
            sleep(1);
            continue;
        }
        else
            fprintf(stdout, "客户端%d连接成功\n",i);

        //给当前客户端连接上锁
        pthread_mutex_lock(&clientsMutex[i]);
        //放入客户端数据
        clients[i].clientfd = clientfd;
        clients[i].addr = addr;
        clients[i].isConn = 1;
        clients[i].index = i;
        //释放
        pthread_mutex_unlock(&clientsMutex[i]);

        //新建一个线程管理当前客户端
        pthread_create(&threadID[i], NULL, (void *)clientManager, &clients[i]);

    }    //结束
}