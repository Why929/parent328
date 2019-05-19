package cn.itcast.core.service;

import cn.itcast.core.pojo.specification.Specification;
import entity.PageResult;
import entity.Result;
import pojogroup.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {
//    Search:页面打开首次加载一些数据:
    public PageResult search(Integer page,Integer rows,Specification specification);

//    add:
    Result add(SpecificationVo specificationVo);

    SpecificationVo findOne(Long id);

    Result update(SpecificationVo specificationVo);

    List<Map> selectOptionList();
}
