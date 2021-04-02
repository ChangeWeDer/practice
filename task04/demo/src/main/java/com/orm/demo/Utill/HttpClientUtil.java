package com.orm.demo.Utill;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;


public class HttpClientUtil {
    
    private static CloseableHttpClient httpClient = null;

    public static void init() {
        //1.创建连接池管理器，默认情况下，此实现将为每个给定路由创建不超过2个并发连接，并且总共不超过20个连接
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(60000,//1.1
                TimeUnit.MILLISECONDS);//tomcat服务器默认支持保持60s的链接，超过60s，会关闭客户端的链接
        connectionManager.setMaxTotal(1000);//设置连接器最多同时支持1000个链接
        connectionManager.setDefaultMaxPerRoute(50);//设置每个路由最多支持50个链接。注意这里路由是指IP+PORT或者指域名

        //2.创建httpclient对象
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .disableAutomaticRetries()
                .build();
    }


    public static String doGet(String url) {
        //初始化连接池
        if(httpClient == null)
            init();
        String httpResponse = null;
        // 创建Get请求
        HttpGet httpGet = new HttpGet(url);
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 配置信息
            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接超时时间(单位毫秒)
                    .setConnectTimeout(5000)
                    // 设置请求超时时间(单位毫秒)
                    .setConnectionRequestTimeout(5000)
                    // socket读写超时时间(单位毫秒)
                    .setSocketTimeout(5000)
                    // 设置是否允许重定向(默认为true)
                    .setRedirectsEnabled(true).build();

            // 将上面的配置信息 运用到这个Get请求里
            httpGet.setConfig(requestConfig);


            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpGet);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            //获取请求体
            httpResponse = EntityUtils.toString(responseEntity,"UTF-8");
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            //回收连接
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return httpResponse;
    }

    public static String doPost(String url) throws IOException {
        if(httpClient == null)
            init();
        String httpResponse = null;
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        // 创建Post请求
        HttpPost httpPost = new HttpPost(url);
        // 响应模型
        CloseableHttpResponse response = null;
        HttpEntity responseEntity = null;
        try {
            // 由客户端执行(发送)Post请求

            response = httpClient.execute(httpPost);
            // 从响应模型中获取响应实体
            responseEntity = response.getEntity();
            if (responseEntity != null) {
                //获取请求体
                httpResponse = EntityUtils.toString(responseEntity,"UTF-8");
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            //回收连接
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return httpResponse;
    }

    public static String doPostWithEntity(String url,String json) throws IOException {
        if(httpClient == null)
            init();
        String httpResponse = null;
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        // 创建Post请求
        HttpPost httpPost = new HttpPost(url);
        // 响应模型
        CloseableHttpResponse response = null;
        HttpEntity responseEntity = null;
        try {
            StringEntity requestEntity = new StringEntity(json,"utf-8");
            requestEntity.setContentEncoding("UTF-8");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(requestEntity);

            // 由客户端执行(发送)Post请求
            response = httpClient.execute(httpPost);
            // 从响应模型中获取响应实体
            responseEntity = response.getEntity();
            if (responseEntity != null) {
                //获取请求体
                httpResponse = EntityUtils.toString(responseEntity,"UTF-8");
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            //回收连接
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return httpResponse;
    }
}

