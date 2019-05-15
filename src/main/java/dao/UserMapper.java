package dao;

import com.meorient.mongodb.Company;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author sunwanghe
 * @date 2019/5/13 17:02
 */
@Mapper
public interface UserMapper {

    @Select("select customer_id,companyname,custentity_meo_address,sales_rep_id,full_name\n" +
            "from customers,employees\n" +
            "where customers.sales_rep_id is not  null and customers.sales_rep_id=employees.employee_id limit #{start},#{length}")
    @Results({
            @Result(id=true,property="extId",column="customer_id"),
            @Result(property="name",column="companyname"),
            @Result(property="address",column="custentity_meo_address"),
            @Result(property="userExtId",column="sales_rep_id"),
            @Result(property="userName",column="full_name"),
    })
    List<Company> selectCompanyList(@Param("start")int start, @Param("length") int length);

    @Select("select count(*)\n" +
            "from customers,employees\n" +
            "where customers.sales_rep_id is not  null and customers.sales_rep_id=employees.employee_id and customers.custentity_meo_address like '杭州%'")
    int getCompanyNum();

}
