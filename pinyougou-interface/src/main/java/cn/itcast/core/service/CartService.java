package cn.itcast.core.service;

import cn.itcast.core.pojo.Cart;
import cn.itcast.core.pojo.item.Item;

import java.util.List;

public interface CartService {

//根据库存对象查询库存对象
    public Item findItemById(Long id);

    public Item selectByPrimaryKey(Long itemId);

    List<Cart> findCartList(List<Cart> oldCartList);

    void mearge(List<Cart> oldCartList, String name);

    List<Cart> findCartListFromRedis(String name);
}
