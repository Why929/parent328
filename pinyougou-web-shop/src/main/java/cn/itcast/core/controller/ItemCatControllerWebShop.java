package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatControllerWebShop {

//    注入ItemCatService层
    @Reference
    ItemCatService itemCatService;

//    根据主parentId查询商品分类层级 数据集合
    /**
     * @param parentId
     * @return
     */
    @RequestMapping("/findByParentId")
    public List<ItemCat> findByParentId(Long parentId){
//    调用Service层
        List<ItemCat> byParentId = itemCatService.findByParentId(parentId);


        return byParentId;//todo:
    }

    //根据id查询item_cat表中的 type_id,查询返回的是一个对象

    /**
     * 实质上是 调用ItemCatService层 ,查询单个ItemCat对象
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public ItemCat findOne(Long id) {
        ItemCat one = itemCatService.findOne(id);
        return one;
    }

    //    查询分类表中的所有信息findItemCatList():给页面放到一个集合中
    @RequestMapping("/findAll")
    public List<ItemCat> itemCatList(){
        return itemCatService.findAll();
    }
}
