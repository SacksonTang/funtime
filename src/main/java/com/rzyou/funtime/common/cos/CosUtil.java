package com.rzyou.funtime.common.cos;

import com.alibaba.fastjson.JSONObject;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.AnonymousCOSCredentials;
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

        // 生成匿名的请求签名，需要重新初始化一个匿名的 cosClient
        // 初始化用户身份信息, 匿名身份不用传入 SecretId、SecretKey 等密钥信息
        COSCredentials cred = new AnonymousCOSCredentials();
        // 设置 bucket 的区域，COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(Constant.TENCENT_YUN_COS_REGION));
        // 生成 cos 客户端
        COSClient cosClient = new COSClient(cred, clientConfig);
        // bucket 名需包含 appid

        GeneratePresignedUrlRequest req =
                new GeneratePresignedUrlRequest(Constant.TENCENT_YUN_COS_BUCKET, key, HttpMethodName.GET);
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
        //CosUtil.upload("123","C:/test/test1.png");
        System.out.println(generatePresignedUrl("123/20191217171038test1.png"));
    }
}
