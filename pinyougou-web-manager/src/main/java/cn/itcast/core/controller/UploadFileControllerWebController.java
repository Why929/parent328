package cn.itcast.core.controller;

import cn.itcast.common.utils.FastDFSClient;
import entity.Result;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
@SuppressWarnings("all")
@RestController
@RequestMapping("/upload")
public class UploadFileControllerWebController {

    //声明变量:访问路径ip:放到了properties文件中:
    // 这个文件通过springmvc.xml context:propety-placeholder 属性加载
    @Value("${FILE_SERVER_URL}")
    private String url;

//    文件上传
    /**
     * @param file 要和页面传参一样
     * @return 成功之后,返回的是url
     * MultpaetFile :可以选择同时上传多个?
     */
    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file) throws Exception {


        try {
//        1.file 中有原始名,二进制等,存的时候使用二进制:获取扩展名
//            file.getOriginalFilename:获得file原始名
//            FilebameUtils.getExtension(name) :获取name扩展名
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
//        2.使用FastDFSClient工具类:上传:没有空参构造(conf配置文件)
//            创建FastDFSClient对象,同时加载 fastDFS配置文件
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
//        3.使用 FastDFSClient.uploadFile(二进制格式.getBytes(),扩展名)
            String filePath = fastDFSClient.uploadFile(file.getBytes(), ext);
            return new Result(true,url+filePath);
        } catch (Exception e) {
//            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}


