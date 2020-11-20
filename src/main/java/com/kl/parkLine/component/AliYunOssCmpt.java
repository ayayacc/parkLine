package com.kl.parkLine.component;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;

@Component
public class AliYunOssCmpt 
{
    @Autowired
    private OSSClient ossClient;
    
    @Autowired
    private Utils utils;

    @Value("${cloud.storage.bucket}")
    private String bucket;

    /**
     * 将输入流上传到OSS
     * @param inputStream 输入流
     * @param uploadKey 上传
     * @return 上传结果
     */
    private PutObjectResult upload(InputStream inputStream, String uploadKey) 
    {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, uploadKey, inputStream, new ObjectMetadata());
        
        PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
        
        ossClient.setObjectAcl(bucket, uploadKey, CannedAccessControlList.Default);

        IOUtils.closeQuietly(inputStream);
        
        return putObjectResult;
    }

    /**
     * 上传文件到OSS
     * @param multipartFile 用户上传的文件
     * @param code oss上的文件代码
     * @return OSS上的文件url
     * @throws IOException
     */
    public void upload(MultipartFile multipartFile, String code) throws IOException 
    {
        upload(multipartFile.getInputStream(), code);
    }
    
    /**
     * 上传本地文件到OSS
     * @param file 文件
     * @param code OSS上的文件代码
     * @return 文件url
     * @throws IOException
     */
    public void upload(String base64Img, String code) throws IOException 
    {
        InputStream is = utils.decodeImg(base64Img);
        upload(is, code);
        is.close();
    }
}
