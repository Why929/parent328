package cn.itcast.core.service;

import cn.itcast.core.mapper.seller.SellerDao;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//商家注册+登录
@Service
public class SellerServiceImpl implements SellerService {
//    注入SellerDao
    @Autowired
    private SellerDao sellerDao;

//商家注册:
    @Override
    public void add(Seller seller) {
//        1.用户名
//        2.密码加密
        /**
         * 使用SpringSecurity框架的BcryptPasswordEncoder
         */
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
//        加密
        String encode = bCryptPasswordEncoder.encode(seller.getPassword());
//        设置回去
        seller.setPassword(encode);
//        3.商家名
//        4.登录状态:新注册设置状态为 0 :需要审核之后才能登录
        seller.setStatus("0");
//        执行保存:
        sellerDao.insertSelective(seller);

    }

//  根据用户名:查询登录用户对象
    @Override
    public Seller findOne(String sellerId) {
//  坑:要点:用户名在数据库表中是 主键:所以使用byPrimaryKey
        Seller seller = sellerDao.selectByPrimaryKey(sellerId);
        return seller;
    }
}
