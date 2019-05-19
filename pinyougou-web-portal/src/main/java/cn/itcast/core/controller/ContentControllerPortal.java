package cn.itcast.core.controller;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
//需要将Controller层注册到dubbo
@RestController
//根据categoryId 查找content表中的 的结果集
@RequestMapping("/content")
public class ContentControllerPortal {

    @Reference
    private ContentService contentService;

    @RequestMapping("/findByCategoryId")
    public List<Content> findBycategoryId(Long categoryId){
        List<Content> byCategoryId = contentService.findByCategoryId(categoryId);
        return byCategoryId;
    }
}
