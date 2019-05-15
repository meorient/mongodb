package com.meorient.mongodb;

import com.alibaba.fastjson.JSONObject;
import com.meorient.http.HttpUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MongodbApplicationTests {

    @Test
    public void contextLoads() {
        String url = "http://api.map.baidu.com/geocoder/v2/?address=杭州富阳区富春街道金平路19号1311室&output=json&ak=yw4wuBm1a6VbICpmz6nXGYY5Eo64YhYo";
        JSONObject jsonObject = HttpUtil.getHttpsResponse(url);
        System.out.println(jsonObject);
        System.out.println(jsonObject.getJSONObject("result").getString("level"));
    }

}
