package com.pinyougou.manager.controller;

import com.pinyougou.common.FastDFSClient;
import entity.Result;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制层
 * @author admin13
 * @version 1.0
 * @description com.pinyougou.shop.controller
 * @date 2018/10/12
 */

@RestController
public class UploadController {

    //引入log控制台输出
    private Logger logger = Logger.getLogger(UploadController.class);

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {
        try {
            //1.获取要上传的图片名
            String oldName = file.getOriginalFilename();
            //2.获取后缀名,不带 .
            String extName = oldName.substring(oldName.lastIndexOf(".") + 1);
            //3、创建FastDFS客户端
            FastDFSClient dfsClient = new FastDFSClient("classpath:fdfs_client.conf");
            //4、上传文件到FastDFS,带后缀名的文件ID
            String fileId = dfsClient.uploadFile(file.getBytes(), extName);
            //5、拼接文件url,文件获取地址
            String url = FILE_SERVER_URL + fileId;
            return new Result(true, url);
        } catch (Exception e) {
            logger.error("文件上传失败,原因是:"+e);
            return new Result(false,"文件上传失败");
        }


    }


}
