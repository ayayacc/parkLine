package com.kl.parkLine.component;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.com.viapi.FileUtils;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.ocr.model.v20191230.RecognizeDrivingLicenseRequest;
import com.aliyuncs.ocr.model.v20191230.RecognizeDrivingLicenseResponse;
import com.aliyuncs.ocr.model.v20191230.RecognizeDrivingLicenseResponse.Data.FaceResult;
import com.kl.parkLine.json.DrivingLicenseVo;

@Component
public class AliYunCmpt 
{
    @Autowired
    private OSS ossClient;
    
    @Autowired
    private DefaultAcsClient acsClient;
    
    @Autowired
    private FileUtils fileUtils;
    
    @Autowired
    private Utils utils;

    @Value("${cloud.storage.bucket}")
    private String bucket;
    
    @Value("${cloud.aliyun.schema}")
    private String schema;
    
    @Value("${cloud.aliyun.oss.endpoint}")
    private String endpoint;

    /**
     * 将输入流上传到OSS
     * @param inputStream 输入流
     * @param uploadKey 上传
     * @return 上传结果
     */
    private PutObjectResult upload(InputStream inputStream, String uploadKey, CannedAccessControlList acl) 
    {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, uploadKey, inputStream, new ObjectMetadata());
        
        PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
        
        ossClient.setObjectAcl(bucket, uploadKey, acl);

        IOUtils.closeQuietly(inputStream);
        
        return putObjectResult;
    }
    
    /**
     * 行驶证识别时，临时将
     * @param code
     * @return
     * @throws IOException 
     * @throws ClientException 
     */
    public String uploadToShanghai(String code) throws ClientException, IOException
    {
        //<Schema>://<Bucket>.<外网Endpoint>/<Object> 
        String tmp = "oss-cn-shenzhen.aliyuncs.com";
        String url = String.format("%s://%s.%s/%s", schema, bucket, tmp, code);
        return fileUtils.upload(url);
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
        upload(multipartFile.getInputStream(), code, CannedAccessControlList.PublicRead);
    }
    
    /**
     * 上传本地文件到OSS
     * @param file 文件
     * @param code OSS上的文件代码
     * @return 文件url
     * @throws IOException
     */
    @Async
    public void upload(String base64Img, String code) throws IOException 
    {
        InputStream is = utils.decodeImg(base64Img);
        upload(is, code, CannedAccessControlList.Private);
        is.close();
    }
    
    /**
     * 将awsOSS的文件下载到InputStream
     * @param key 文件代码
     * @return
     */
    public InputStream downloadStream(String key)
    {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);

        OSSObject OSSObject = ossClient.getObject(getObjectRequest);

        return OSSObject.getObjectContent();
    }
    
    /**
     * 获取awsOSS上的文件数据
     * @param key 文件代码
     * @return
     * @throws IOException
     */
    public byte[] getBytes(String key) throws IOException 
    {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);
        HttpHeaders httpHeaders = new HttpHeaders();
        OSSObject OSSObject = ossClient.getObject(getObjectRequest);
        InputStream objectInputStream = OSSObject.getObjectContent();
        byte[] bytes = IOUtils.toByteArray(objectInputStream);
        objectInputStream.close();
        httpHeaders.setContentLength(bytes.length);
        return bytes;
    }
    
    /**
     * 识别行驶证信息
     * @param code oss中代码
     * @return
     * @throws ServerException
     * @throws ClientException
     * @throws ParseException 
     * @throws IOException 
     */
    public DrivingLicenseVo recognizeDrivingLicense(String code) throws ServerException, ClientException, ParseException, IOException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String imgUrl = uploadToShanghai(code);
        RecognizeDrivingLicenseRequest req = new RecognizeDrivingLicenseRequest();
        req.setSide("face"); //首页
        req.setImageURL(imgUrl);
        RecognizeDrivingLicenseResponse resp = acsClient.getAcsResponse(req);
        FaceResult faceResult = resp.getData().getFaceResult();
        DrivingLicenseVo drivingLicenseVo = DrivingLicenseVo.builder().address(faceResult.getAddress())
            .engineNumber(faceResult.getEngineNumber())
            .issueDate(sdf.parse(faceResult.getIssueDate()))
            .model(faceResult.getModel())
            .owner(faceResult.getOwner())
            .plateNumber(faceResult.getPlateNumber())
            .registerDate(sdf.parse(faceResult.getRegisterDate()))
            .useCharacter(faceResult.getUseCharacter())
            .vehicleType(faceResult.getVehicleType())
            .vin(faceResult.getVin()).build();
        return drivingLicenseVo;
    }
    
}
