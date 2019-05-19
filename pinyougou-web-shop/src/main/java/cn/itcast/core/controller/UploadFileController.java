package cn.itcast.core.controller;

import cn.itcast.common.utils.FastDFSClient;
import entity.Result;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadFileController {

    //        声明变量:访问路径ip
    @Value("${FILE_SERVER_URL}")
    private String url;

//    文件上传
    /**
     * @param file 要和页面传参一样
     * @return 成功之后,返回的是url
     */
    @RequestMapping("uploadFile")
    public Result uploadFile(MultipartFile file) throws Exception {


        try {
//        file 中有原始名,二进制等,存的时候使用二进制:获取扩展名
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
//        使用FastDFSClient工具类:上传:没有空参构造(conf配置文件)
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
//
            String filePath = fastDFSClient.uploadFile(file.getBytes(), ext);
            return new Result(true,url+filePath);
        } catch (Exception e) {
//            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}


