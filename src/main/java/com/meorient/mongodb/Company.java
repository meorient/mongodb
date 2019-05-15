package com.meorient.mongodb;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author sunwanghe
 * @date 2019/5/13 14:16
 */

@Data
@Document(collection = "customers")
public class Company {

    @Id
    private String extId;//客户的外部id
    @Field("name")
    private String name;//公司名
    @Field("address")
    private String address;
    @Field("latitude")
    private String latitude;//纬度
    @Field("longitude")
    private String longitude;//经度
    @Field("userExtId")
    private String userExtId;//分配给销售员的外部id
    @Field("userName")
    private String userName;//销售员姓名
    @Field("distance")
    private String distance;//距离
    @Field("level")
    private String level;//
    @Field("confidence")
    private String confidence;
    @Field("comprehension")
    private String comprehension;
    @Field("precise")
    private String precise;
    @Field("location")
    private Location location;

}
@Data
class Location{
    Location(String type ,Double []  coordinates){
        this.type=type;
        this.coordinates=coordinates;
    }
    private String type;
    private Double [] coordinates;
}
