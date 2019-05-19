package cn.itcast.core.service;

import cn.itcast.core.pojo.good.Goods;
import entity.PageResult;
import entity.Result;
import pojogroup.GoodsVo;

public interface GoodsService {
    //新增商品
    void add(GoodsVo vo);
//  Goods表查询:分页查询+条件查询
    PageResult search(Integer page, Integer rows, Goods goods);

    //商品修改之 信息回显 根据id查询商品
    GoodsVo findOne(Long id);
//    商品修改按钮触发,信息回显修改之后,的保存操作
    void update(GoodsVo vo);

//    商品审核:删除  支持批量
    void delete(Long[] ids);
//    商品审核:同意/不同意  支持批量
    void updateStatus(Long[] ids, String status);
}
