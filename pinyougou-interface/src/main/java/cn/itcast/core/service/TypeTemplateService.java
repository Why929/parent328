package cn.itcast.core.service;

import cn.itcast.core.pojo.template.TypeTemplate;
import entity.PageResult;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

public interface TypeTemplateService {
//    search:首页加载:包含条件查询:
    public PageResult search(Integer page, Integer rows, @RequestBody TypeTemplate typeTemplate );

//  add 新增一条TypeTemplate表中的一条记录
    void add(TypeTemplate typeTemplate);

//  回显:修改按钮触发的操作
    TypeTemplate findOne(Long id);

//   更新:update
    void update(TypeTemplate typeTemplate);


    List<Map> findBySpecList(Long id);
}
