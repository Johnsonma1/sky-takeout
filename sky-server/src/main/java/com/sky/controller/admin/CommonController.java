package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    /**
     * 文件上传
     */
    @ApiOperation("文件上传")
    @PostMapping("/upload")
    @SneakyThrows
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file);
        //上传图片到阿里云：文件字节数组，文件名
        byte[] bytes = file.getBytes();
        String originalFilename = file.getOriginalFilename(); //abc.jpg
        String uuidFilename = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String url = AliOssUtil.upload(bytes, uuidFilename);
        return Result.success(url);
    }

}
