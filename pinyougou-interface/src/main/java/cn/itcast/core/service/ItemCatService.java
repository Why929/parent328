package cn.itcast.core.service;

import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;

//商品分类管理
public interface ItemCatService {
//   根据一级商品分类id查询 查询
    public List<ItemCat> findByParentId(Long parent);

//    根据id查询type_id   查询的是item_cat表
    public ItemCat findOne(Long id);

//    查询分类表中的所有信息findItemCatList():给页面放到一个集合中
    List<ItemCat> findAll();
}
