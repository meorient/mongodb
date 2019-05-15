package com.meorient.mongodb;

import com.alibaba.fastjson.JSONObject;
import com.meorient.http.HttpUtil;
import dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
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
    @RequestMapping(value = "/start")
    public String insert(){

        int count = userMapper.getCompanyNum();
        int num = count/6000 + 1;
        for(int i = 0;i<1;i++){
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
        return "success";
    }


}
class SearchThread implements Runnable{
    private MongoTemplate mongoTemplate;

    private List<Company> list ;
    private String url = "http://api.map.baidu.com/geocoder/v2/?address=ADDRESS&output=json&ak=yw4wuBm1a6VbICpmz6nXGYY5Eo64YhYo";//小孙的ak
    //private String url = "http://api.map.baidu.com/geocoder/v2/?address=ADDRESS&output=json&ak=13sRQX9y3U8e8IaaE9YGwCq9N4jklQQu";//小周的ak
    public SearchThread(List<Company> list,MongoTemplate mongoTemplate){
        this.list = list;
        this.mongoTemplate= mongoTemplate;
    }
    @Override
    public void run() {
        for(Company company: list){
            if(company.getAddress()!=null) {
                url = url.replace("ADDRESS", company.getAddress());
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

            System.out.println("一次完成");
        }
        System.out.println("一条线程执行完成");
    }
}
