package cn.itcast.core.controller;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//商户注册
@RestController
@RequestMapping("/seller")
public class SellerController {
//    引入dubbo 需要在xml中配置了才会生效
    @Reference
    private SellerService sellerService;

//    商家注册
    @RequestMapping("/add")
    public Result add(@RequestBody Seller seller){
        try {
//    调用service层 ->去interface中写->sellerGoods中实现->回来注入
            sellerService.add(seller);
            return new Result(true,"注册成功");
        } catch (Exception e) {
//            e.printStackTrace();
            return new Result(false,"注册失败");
        }
    }
}
