import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.orm.demo.dynamic.SQLUpdateDoing;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.IOException;
import java.util.*;

import static com.orm.demo.Utill.HttpClientUtil.doGet;

/**
 * Created by Admin on 2021/3/26.
 */
public class test {
    private static final Logger log = LoggerFactory.getLogger(test.class);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${JsonToSqlUtill.field}")
    private String field;

    @Value("${JsonToSqlUtill.timeField}")
    public String timeField;

    @Test
    public void insertData() throws IOException {
        List<Object> list = new ArrayList<>();
        System.out.println(timeField);
        String url = "https://gymbot.cn/pushlink/all";
        String jsonPath = "$.data[*]";

//        String httpResponse = doPostWithEntity(url,"{\n" +
//                "  \"pageNo\": 1,\n" +
//                "  \"pageSize\": 10\n" +
//                "}");
        String httpResponse = doGet("http://localhost:8081/");


        String table = jsonPath.substring(jsonPath.lastIndexOf(".") + 1, jsonPath.lastIndexOf("["));
        //获取接口data
        List<HashMap> data = JsonPath.read(httpResponse, jsonPath);

        StringBuilder updateFields = new StringBuilder();
        for (Object key : data.get(0).keySet()){
            list.add(key.toString());
            updateFields.append(key).append("=").append("values(").append(key).append("),");
        }
        //去除最后的逗号
        updateFields.delete(updateFields.length()-1,updateFields.length()).append(";");
        //System.out.println(updateFields);

        //获取values里要插入的字段
        System.out.println(list);
        //索引，用作判断循环中的map中，某个字段存在空值，就用null代替
        int index = 0;
        StringBuilder values = new StringBuilder();
        for (Map map : data){
            values.append("(");
            index = 0;
            for (Object key : map.keySet()){
//                System.out.println(list.indexOf(key)+" "+index);
                //发现索引不对，则自动给缺失的数据段填充null值
                    while (list.indexOf(key) != index){
                        values.append("'").append("null").append("'").append(",");
                        index++;
                    }
                    index++;
                values.append("'").append(map.get(key)).append("'").append(",");
            }
            //最后在给末尾缺失的数据段填充null值
            while (list.size() != index){
                values.append("'").append("null").append("'").append(",");
                index++;
            }
            //去除最后一个字段的逗号
            values.delete(values.length()-1,values.length());
            values.append("),");
        }
        //去除最后的逗号
        values.delete(values.length()-1,values.length());

        //最后的拼接需要去掉最后一个逗号
        StringBuilder tableAndField = new StringBuilder();
        tableAndField.append(table).append("(").append(field).append(")").
                append("VALUES").append(values).append("ON DUPLICATE KEY UPDATE name=values(name)");

        System.out.println(tableAndField);


    }

    @Test
    public void Data() {
        SQLUpdateDoing<Object> result = (sql, fields, params, typeReferenceMap) -> {
            sql.append("select GROUP_CONCAT(COLUMN_NAME) from information_schema.COLUMNS where table_name = 'data';");
        };
        result.go(Object.class, jdbcTemplate, log, objectMapper);
        System.out.println();
    }
}
