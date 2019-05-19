package cn.itcast.core.controller;

import cn.itcast.core.service.ItemsearchService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/itemsearch")
public class ItemsearchController {
//    向zookeeper申请使用service变量
    @Reference
    ItemsearchService itemsearchService;

    @RequestMapping("/search")
    public Map<String,Object> search(@RequestBody Map<String,String> searchMap){

        Map<String, Object> resultMap = itemsearchService.search(searchMap);

        return resultMap;
    }
}
