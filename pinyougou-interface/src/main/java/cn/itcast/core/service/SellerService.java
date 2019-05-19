package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
//商家注册+登录
public interface SellerService {
//    商家注册:
public void add(@RequestBody Seller seller);

//根据用户名查询 登录用户
//    注意:这里用户名是 String sellerId :是主键,在表中 是主键!!!!!
    Seller findOne(String sellerId);
}
