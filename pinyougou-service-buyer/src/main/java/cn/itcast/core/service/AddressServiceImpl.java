package cn.itcast.core.service;

import cn.itcast.core.mapper.address.AddressDao;
import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.pojo.address.AddressQuery;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    AddressDao addressDao;

    @Override
    public List<Address> findListByLoginUser(String name) {
        //1.创建条件查询对象
        AddressQuery addressQuery = new AddressQuery();
        AddressQuery.Criteria criteria = addressQuery.createCriteria();
        criteria.andUserIdEqualTo(name);
//        2.执行查询
        List<Address> addressesList = addressDao.selectByExample(addressQuery);
        return addressesList;
    }
}
