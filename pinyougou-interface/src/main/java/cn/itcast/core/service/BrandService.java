package cn.itcast.core.service;

import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

import entity.PageResult;
import entity.Result;

public interface BrandService {

    //查询所有商品 原始
    public List<Brand> findAll();

//    查询findPage分页对象:总条数:查询结果集
    public PageResult findPage(Integer pageNum, Integer pageSize);

//    新增brand
    public Result add(Brand band);

    public Brand findOne(Long id);

    public void update(Brand brand);

    void delete(long ids);

    PageResult search(Integer pageNum, Integer pageSize, Brand brand);

    List<Map> selectOptionList();
}
