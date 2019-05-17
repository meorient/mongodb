package com.meorient.mongodb;

import com.alibaba.fastjson.JSONObject;
import com.meorient.http.HttpUtil;
import dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * @author sunwanghe
 * @date 2019/5/13 14:03
 */
@RestController
public class Controller {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Resource
    private RedisTemplate<String, Company> redisTemplate;

    @RequestMapping(value = "/mongo")
    public String insert(){

        int count = userMapper.getCompanyNum();
        int num = count/6000 + 1;
        for(int i = 0;i<num;i++){
            List<Company> compayList = userMapper.selectCompanyList(i*6000,6000);
            List<Company> list1 = new LinkedList<>();
            List<Company> list2 = new LinkedList<>();
            List<Company> list3 = new LinkedList<>();
            List<Company> list4 = new LinkedList<>();
            List<Company> list5 = new LinkedList<>();
            int a = compayList.size()/5;
            for(int j=0;j<compayList.size();j++){
                if(j<a){
                    list1.add(compayList.get(j));
                }else if(j>=a&&j<2*a){
                    list2.add(compayList.get(j));
                }else if(j>=2*a&&j<3*a){
                    list3.add(compayList.get(j));
                }else if(j>=3*a&&j<4*a){
                    list4.add(compayList.get(j));
                }else {
                    list5.add(compayList.get(j));
                }
            }
            new Thread(new SearchThread(compayList,mongoTemplate)).start();
        }
        return "Insert to mongo success";
    }

    @RequestMapping(value = "/redis")
    public String insertToRedis(){
        List<Company> companies = mongoTemplate.findAll(Company.class);

        new Thread(()->{
            for(Company company:companies){
                System.out.println(company.getName());
                GeoOperations<String,Company> ops = redisTemplate.opsForGeo();
                ops.add("customers", new RedisGeoCommands.GeoLocation(company.getName(),new Point(company.getLocation().getCoordinates()[0],company.getLocation().getCoordinates()[1])));
            }
        }).start();
        return "Insert to redis success";
    }

    @RequestMapping(value = "/h2")
    public String insertToH2(){
        List<Company> companies = mongoTemplate.findAll(Company.class);
        return "Insert to h2 success";
    }


}
class SearchThread implements Runnable{
    private MongoTemplate mongoTemplate;

    private List<Company> list ;
    private String url3 = "http://api.map.baidu.com/geocoder/v2/?address=";
    private String url2 = "&output=json&ak=LG9WZRIQGZ7l3ZL3cg0lh5rE28DBCytv";//储蓄的ak
//    private String url2 = "&output=json&ak=yw4wuBm1a6VbICpmz6nXGYY5Eo64YhYo";//小孙的ak
//    private String url2 = "&output=json&ak=13sRQX9y3U8e8IaaE9YGwCq9N4jklQQu";//小周的ak

    public SearchThread(List<Company> list,MongoTemplate mongoTemplate){
        this.list = list;
        this.mongoTemplate= mongoTemplate;
    }
    @Override
    public void run() {

        int i = 1;
        for(Company company: list){
            if(company.getAddress()!=null) {
                String url = url3+company.getAddress()+url2;
                JSONObject jsonObject = HttpUtil.getHttpsResponse(url);
                if(jsonObject!=null) {
                    JSONObject result = jsonObject.getJSONObject("result");
                    if (result != null) {
                        company.setLevel(result.getString("level"));
                        company.setPrecise(result.getString("precise"));
                        company.setComprehension(result.getString("comprehension"));
                        company.setConfidence(result.getString("confidence"));
                        String longitude = result.getJSONObject("location").getString("lng");
                        String latitude = result.getJSONObject("location").getString("lat");
                        company.setLatitude(latitude);
                        company.setLongitude(longitude);
                        company.setLocation(new Location("Point",new Double[]{Double.parseDouble(longitude),Double.parseDouble(latitude)} ));
                        mongoTemplate.save(company);
                    }else{
                        String message = jsonObject.getString("message");
                        System.out.println(message);
                    }
                }
            }

            System.out.println("第"+i+"次请求完成");
            i++;
        }
        System.out.println("一条线程执行完成");
    }
}
