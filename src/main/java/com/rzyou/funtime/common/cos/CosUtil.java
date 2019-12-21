package com.rzyou.funtime.common.cos;

import com.alibaba.fastjson.JSONObject;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.Date;

public class CosUtil {

    public static String generatePresignedUrl(String key){
        if (StringUtils.isBlank(key)) return key;
        // 初始化永久密钥信息
        COSCredentials cred = new BasicCOSCredentials(Constant.TENCENT_YUN_COS_SECRETID, Constant.TENCENT_YUN_COS_SECRETKEY);
        Region region = new Region(Constant.TENCENT_YUN_COS_REGION);
        ClientConfig clientConfig = new ClientConfig(region);
        // 生成 cos 客户端。
        COSClient cosClient = new COSClient(cred, clientConfig);
        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        GeneratePresignedUrlRequest req =
                new GeneratePresignedUrlRequest(Constant.TENCENT_YUN_COS_BUCKET, key, HttpMethodName.GET);
        // 设置签名过期时间(可选), 若未进行设置, 则默认使用 ClientConfig 中的签名过期时间(1小时)
        // 这里设置签名在半个小时后过期
        Date expirationDate = new Date(System.currentTimeMillis() + 30L * 60L * 1000L);
        req.setExpiration(expirationDate);
        URL url = cosClient.generatePresignedUrl(req);
        cosClient.shutdown();
        return url.toString();
    }

    public static COSClient initCosClient(){
        // 1 初始化用户身份信息（secretId, secretKey）。
        COSCredentials cred = new BasicCOSCredentials(Constant.TENCENT_YUN_COS_SECRETID, Constant.TENCENT_YUN_COS_SECRETKEY);
        // 2 设置 bucket 的区域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        Region region = new Region(Constant.TENCENT_YUN_COS_REGION);
        ClientConfig clientConfig = new ClientConfig(region);
        // 3 生成 cos 客户端。
        COSClient cosClient = new COSClient(cred, clientConfig);
        return cosClient;
    }

    public static void upload(String userId,String path){
        try {
            // 指定要上传的文件
            File localFile = new File(path);
            // 指定要上传到的存储桶
            // 指定要上传到 COS 上对象键
            String key = userId+"/"+ DateUtil.getCurrentDateTime()+localFile.getName();
            PutObjectRequest putObjectRequest = new PutObjectRequest(Constant.TENCENT_YUN_COS_BUCKET, key, localFile);
            COSClient cosClient = initCosClient();
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            System.out.println(JSONObject.toJSONString(putObjectResult));
        } catch (CosServiceException serverException) {
            serverException.printStackTrace();
        } catch (CosClientException clientException) {
            clientException.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CosUtil.upload("123","C:/test/test1.png");
        //System.out.println(generatePresignedUrl("user/test.jpeg"));
    }
}
