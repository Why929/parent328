package cn.itcast.core.service;

import cn.itcast.core.mapper.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {
//    注入Dao
    @Autowired
    ItemCatDao itemCatDao;
//    注入redis
    @Autowired
    RedisTemplate redisTemplate;

//    页面初始化:点击商品分类:页面跳转:初始化:加载所有父id=0的一级分类:根据主parentId查询商品分类层级 数据集合,
//    查询商品分类:根据分类层级id
    @Override
    public List<ItemCat> findByParentId(Long parent) {
//        页面初始化:查询所有分类信息 放到redis中
//        1.查询所有分类
        List<ItemCat> allItemCategory = findAll();
//        2.放入redis中格式:目标:能根据分类name查到分类id, 分类name:分类模板id    id parent_id name type_id
//        循环放入rendis
        for (ItemCat itemCat : allItemCategory) {
//                                     redis_Map_key         分类名称            本表中分类name对应的模板id type_id
            redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());
        }
        /**
         * 要点:关于入参是什么类型:看查询的情况:如果查询入参是 返回参数类型种的变量,
         * 就要去返回参数对象中查看 对应入参变量类型,保持一致
         */
        /**
         * 没有进行分页管理:思路:增加分页标签:增加分页config:/todo:有时间就去做
         */
//        调用Dao层:分级parent_id 是long类型,但不是主键,所以要使用条件对象进行查询
        ItemCatQuery itemCatQuery = new ItemCatQuery();
//        将查询条件赋值到 条件对象中
        itemCatQuery.createCriteria().andParentIdEqualTo(parent);
//        进行条件查询
        List<ItemCat> itemCatList = itemCatDao.selectByExample(itemCatQuery);
        return itemCatList;
    }

    @Override
    public ItemCat findOne(Long id) {
        ItemCat itemCat = itemCatDao.selectByPrimaryKey(id);
        return itemCat;
    }

//    查询分类表中的所有信息findItemCatList():给页面放到一个集合中
    @Override
    public List<ItemCat> findAll() {
//        .selectByExample( agr ) 当参数为null的时候,表示查询所有数据
        List<ItemCat> itemCatList = itemCatDao.selectByExample(null);
        return itemCatList;
    }
}
