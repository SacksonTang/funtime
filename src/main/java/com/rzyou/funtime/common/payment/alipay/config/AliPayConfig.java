package com.rzyou.funtime.common.payment.alipay.config;

public abstract class AliPayConfig {

    /**
     * 请填写您的支付类接口异步通知接收服务地址，例如：https://www.test.com/callback
     * @return
     */
    abstract String getNotifyUrl();

    /**
     * 请填写您的应用公钥证书文件路径，例如：/foo/appCertPublicKey_2019051064521003.crt
     * @return
     */
    abstract String getMerchantCertPath();

    /**
     * 请填写您的支付宝公钥证书文件路径，例如：/foo/alipayCertPublicKey_RSA2.crt
     * @return
     */
    abstract String getAlipayCertPath();

    /**
     * 请填写您的支付宝根证书文件路径，例如：/foo/alipayRootCert.crt
     * @return
     */
    abstract String getAlipayRootCertPath();

    public String getProtocol(){
        return "https";
    }

    public String getGatewayHost(){
        return "openapi.alipay.com" ;
    }
    public String getServerUrl(){
        return "https://openapi.alipay.com/gateway.do" ;
    }
    public String getSignType(){
        return "RSA2";
    }

    /**
     * 请填写您的AppId，例如：2019091767145019
     * @return
     */
    public String getAppId(){
        return "2021001172638147";
    }

    /**
     * 请填写您的应用私钥，例如：MIIEvQIBADANB
     * @return
     */
    public String getMerchantPrivateKey(){
        return "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDQWidn5qhhDYWSXZQcaXeW4w+gYq/W6kqmHhETILetG9bkin9jTO9nreaApudwfDCmaSSULjsAkbTCOtggZ5F+ag9vqMX3DHS4MbaavTDBavjOTFEIcHaC6ip4MHP5pn7tFcccEipA4bRT7JLhyesNzMy7MPpANDLkByTd1ZNbESG5yU2PkSxEWkIdFPBuoIhxjvkEJkw4swNmtKSDMwuKgqBakJlFCoUFX2elxXZxS+3BYwQ4gqLZsOSzPQHIFZ7RE8zC5eEfTvgJ45qZJIgWPiBnpmiCKdlgzrIOpimVh66M3f199WWld4lwkcs51we2W/WjI+lyHWNLEKwXgaxjAgMBAAECggEASW9tMi7XLBXy/UqkWHtH3lO7f0EpTuuXUgOI6x/9/TKxxC2YhqxoOTfO5YtuSXRMRWsLrHq66xFwD4FkCc/XNSI2vJlU5SVrwjwra1DBbXrBpRy69umEq9HTFDenuNdZJ0Ekw50XbXTf9TCN+K0NfvivNE8n9Rcw1LQ750BOxqqZKeUsv9UIzblgVXcBNFjgQLPgEdfrLzC4SDvZ4Q5E140qUWM8v9ssgTjpIZ4nCQk3fsAFzsxrr8GkFE1vqqEChDaE4PKZloiGcn36SMuw4LJYOSvpp86vmkk5Bptp5m5TiWaenXNdLzK2A6PVC1bVrjD+taRHVAKRRxs6YZkoWQKBgQDx0JwaYar43VGRkMzIz5bl0m1XwogS04+STV0DZRv3OT7WHWPNIQKbslfHYnCDL7vLa1+v21GIn98kFzYCUzrOcWYarFBmPu0zdeiCrlLFqq549OVOqwB/TFyBPNwGmmj6KW2TGRQFbbafE50dfgRWgHVVuEDoPyQYgoQBpAkHfQKBgQDckwbYtfyJWgsBWpr1H9nwibIpjgtxt1+VISSgGVWCb/xuA3BbMhLVwkjjaEz5eK+MO1px2Jhxi3N8B7k95VZDTJf49QUkxfWcmjoOkOIfBe4XR0Ofyhovp83Ru0aUOdtVRLR5dz6kHY4e+AyZQxEh0mdaCKqJ22kJPmXLFJ6JXwKBgQDRxt2FbXE7EBSxKWLeWmhX2+MLnTEa0/nbO84TIeXO5RSD/18TFy4CEAdiax2cUQ6je/S4xJTI55hW4OPzxGxhp7KiNRZSfQid+g3JQJ2B6X4OZLWBN4vo4z/kxf1CpTWov8PVNo5N303sVsGYUjhFKo+f24ZDdHNF6APIwJssqQKBgQCvzDrTs8/vhKVq0dTY8ybXMFegCQcT/gufJwgy1Z4yVKDC6di9xl3+oldHb9SRrF91i8SdIfz12igTvXAlZchn4P7qvBX6ZhBIXR9ShqTCBw7o3LKIAyL1wm8m7nTlJGusPRL+EE0LrkacZiRcbniUFUasGmYZc5b2eBXXUrq8TwKBgGtat6u2Ne/4nt7vM08PBIj05aja/44ubbnNHmgsEcWwphdbm/FBeRnTLzZqTVSyKnH9ruvQ9mUJFymKeSxwlfd7OMCK65Yzk2xC19uMn4heEYNPsFj9naUvv3SimTMlwCTBdbt1+fhLIW6Hn1qn6Gnz/iBQ1JVlL9pmtHS0ErhD";
    }

    /**
     * 请填写您的支付宝公钥，例如：MIIBIjANBg
     * @return
     */
    public String getAlipayPublicKey(){
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0FonZ+aoYQ2Fkl2UHGl3luMPoGKv1upKph4REyC3rRvW5Ip/Y0zvZ63mgKbncHwwpmkklC47AJG0wjrYIGeRfmoPb6jF9wx0uDG2mr0wwWr4zkxRCHB2guoqeDBz+aZ+7RXHHBIqQOG0U+yS4cnrDczMuzD6QDQy5Ack3dWTWxEhuclNj5EsRFpCHRTwbqCIcY75BCZMOLMDZrSkgzMLioKgWpCZRQqFBV9npcV2cUvtwWMEOIKi2bDksz0ByBWe0RPMwuXhH074CeOamSSIFj4gZ6ZoginZYM6yDqYplYeujN39ffVlpXeJcJHLOdcHtlv1oyPpch1jSxCsF4GsYwIDAQAB";
    }

    /**
     * 请填写您的AES密钥，例如：aa4BtZ4tspm2wnXLb1ThQA==
     * @return
     */
    public String getEncryptKey(){
        return "zPat8+ns67CKMUL8i4Xo0Q==";
    }

}
