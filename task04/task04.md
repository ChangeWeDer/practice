## 一、需求
我们无法通过账号密码去访问客户的数据库，但是我们可以通过访问客户提供的接口去获取数据，因此，我们可以通过请求接口的方式，对返回的json数据进行解析，然后就能得到我们所需要的数据。

1. 通过配置文件的方式配置所需要信息，增强灵活性

2. 请求到的数据，如果在数据库存在就更新，不存在就插入

3. 如果json中某个数据不存在，则自动补全，并且置为空值，存入数据库中

4. 返回的数据如果存在时间戳，需要进行解析再存入到数据库中

5. 使用http连接池，减低频繁进行连接断开的开销

## 二、思路

工具包：

Httpclient：发送http请求

jsonPath：用于解析字符串

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2d0c76df186d4afd9d6f9fedf4dd78da~tplv-k3u1fbpfcp-watermark.image)

## 三、HttpClient工具使用

引入maven依赖：


```xml
<!-- httpClient -->
<dependency>
	<groupId>org.apache.httpcomponents</groupId>
	<artifactId>httpclient</artifactId>
	<version>4.5.5</version>
</dependency>
```

新增一个HttpClientUtil工具类来使用：
```Java
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
```

## 四、JsonPath工具使用
引入maven：

```xml
<!-- jsonPath json提取工具 -->
<dependency>
	<groupId>com.jayway.jsonpath</groupId>
	<artifactId>json-path</artifactId>
	<version>2.4.0</version>
</dependency>
```

新增一个通过jsonPath工具解析json后生成插入SQL的工具类JsonToSqlUtill

```java

@Component
public class JsonToSqlUtill {
    @Value("${JsonToSqlUtill.field}")
    private Set<Object> field;

    private static Set<Object> s_field;

    @Value("${JsonToSqlUtill.timeField}")
    private Set<Object> timeField;

    private static Set<Object> s_timeField;

    @Value("${JsonToSqlUtill.timeFieldType}")
    private boolean timeFieldType;

    private static boolean s_timeFieldType;

    @Value("${JsonToSqlUtill.tableName}")
    private String tableName;

    private static String s_tableName;

    @Value("${JsonToSqlUtill.dataPath}")
    private String dataPath;

    private static String s_dataPath;

    @Value("${JsonToSqlUtill.url}")
    private String url;

    private static String s_url;

    @PostConstruct
    public void getField(){
        s_field = this.field;
    }

    @PostConstruct
    public void getTimeField(){
        s_timeField = this.timeField;
    }

    @PostConstruct
    public void getTimeFieldType(){
        s_timeFieldType = this.timeFieldType;
    }

    @PostConstruct
    public void getTableName(){
        s_tableName = this.tableName;
    }
    @PostConstruct
    public void getDataPath(){
        s_dataPath = this.dataPath;
    }
    @PostConstruct
    public void getUrl(){
        s_url = this.url;
    }

    public static String getSqlWithJson(String json) throws IOException {
        //获取httpclient返回数据
        String httpResponse = doPostWithEntity(s_url,json);
        return getSql(httpResponse);
    }

    public static String getSqlWithGet() throws IOException {
        //获取httpclient返回数据
        String httpResponse = doGet(s_url);
        return getSql(httpResponse);
    }

    public static String getSqlWithPost() throws IOException {
        //获取httpclient返回数据
        String httpResponse = doPost(s_url);
        return getSql(httpResponse);
    }

    private static String getSql(String httpResponse) throws IOException {
        List<Object> list = new ArrayList<>();
        //获取json中所需要的数据
        List<HashMap> data = JsonPath.read( httpResponse,"$."+ s_dataPath +"[*]");


        //ON DUPLICATE KEY UPDATE（sql：如果数据库存在就更新，不存在就插入） 后的动态数据
        StringBuilder updateFields = new StringBuilder();
        for (Object key : s_field){
            //顺便将字段加入到list集合中，方便后面的字段判空对比
            list.add(key.toString());
            updateFields.append(key).append("=").append("values(").append(key).append("),");
        }
        //去除最后的逗号
        updateFields.delete(updateFields.length()-1,updateFields.length()).append(";");

        //获取values里要插入的字段
        //索引，用作判断循环中的map中，某个字段存在空值，就用null代替
        int index = 0;
        StringBuilder values = new StringBuilder();
        for (Map map : data){
            //每次循环置0
            index = 0;
            values.append("(");
            for (Object key : map.keySet()){
                //发现索引不对，则自动给缺失的数据段填充null值，直到index相等为止
                while (list.indexOf(key) != index){
                    values.append("'").append("null").append("'").append(",");
                    index++;
                }
                //如果值是时间，需要进行格式转换
                if (s_timeField.contains(key)){
                    if (s_timeFieldType){
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String date = df.format(new Date(Long.parseLong((map.get(key).toString()))));
                        //写入时间，跳过后面的执行步骤
                        values.append("'").append(date).append("'").append(",");
                    }else {
                        values.append("'").append(map.get(key).toString()).append("'").append(",");
                    }
                    index++;
                    continue;
                }

                //将值加入到values中
                values.append("'").append(map.get(key)).append("'").append(",");
                //进入到下一个索引中
                index++;
            }
            //最后在给末尾缺失的数据段填充null值
            while (list.size() != index){
                //如果缺少的是时间，取默认时间
                if (s_timeField.contains(list.get(index))){
                    //写入时间，跳过后面的执行步骤
                    values.append("'").append("0000-00-00").append("'").append(",");
                    index++;
                    continue;
                }
                values.append("'").append("null").append("'").append(",");
                index++;
            }
            //去除最后一个字段的逗号
            values.delete(values.length()-1,values.length());
            values.append("),");
        }
        //去除生成在末尾的逗号
        values.delete(values.length()-1,values.length());

        //最后的拼接
        StringBuilder finallySql = new StringBuilder();
        finallySql.append("INSERT INTO ").append(s_tableName).append(" (").append(s_field.toString().substring(1,s_field.toString().length()-1)).append(")").
                append("VALUES").append(values).append("ON DUPLICATE KEY UPDATE ").append(updateFields);

        return String.valueOf(finallySql);
    }
}
```

使用：
首先要配置文件（这里使用的是yml格式）

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7821c2558b4d491f9d3530b1460aff5c~tplv-k3u1fbpfcp-watermark.image)


```yml
JsonToSqlUtill:
  field: id,name,description,pictureUrl,url,createDate #数据字段名，按返回顺序写(英文,隔开)
  timeField: createDate,updateDate #格式是时间的字段(英文,隔开)
  timeFieldType: true #是否为时间戳格式
  tableName: data #数据库表名
    #填写所需要数据data的路径 例如：{"store": { "data": [{"category": "reference"，"price": 8.95}，路径为：store.data
  dataPath: data.data
  url: https://xxxxxxxxxx #url
```

配置好文件后，提供了3个可直接使用的方法

getSqlWithGet()：直接使用配置文件的url进行Get请求，返回一个完整的SQL

getSqlWithPost()：直接使用配置文件的url进行Post请求，返回一个完整的SQL

getSqlWithJson(json)：附带一个json字符串进行请求，返回一个完整的SQL

```java
        //通过Get方式请求
        String Sql = getSqlWithGet();
        System.out.println(Sql);

        //通过Post方式请求
        String Sql = getSqlWithPost();
        System.out.println(Sql);

        //通过Post+Json参数请求
        String Sql = getSqlWithJson(json);
        System.out.println(Sql);

```

如图所示：

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/093ac563e00c478f8ae7bd311d11fc4f~tplv-k3u1fbpfcp-watermark.image)

在数据库创建好对应的数据表，最后使用自己的方式执行这段SQL即可。
