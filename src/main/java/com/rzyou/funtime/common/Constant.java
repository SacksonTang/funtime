package com.rzyou.funtime.common;


public class Constant {

    public final static String COS_URL_PREFIX = "http://funtime-1300805214.cos.ap-shanghai.myqcloud.com";
    public final static String AGREEMENT_PRIVACY = "/agreement/privacy_protocol.html";
    public final static String AGREEMENT_WITHDRAL = "/agreement/withdral_protocol.html";
    public final static String AGREEMENT_RECHARGE = "/agreement/recharge_protocol.html";
    public final static String AGREEMENT_USER = "/agreement/user_protocol.html";
    public final static String DEFAULT_MALE_HEAD_PORTRAIT = "/default/head1.png";
    public final static String DEFAULT_FEMALE_HEAD_PORTRAIT = "/default/head2.png";
    public final static String DEFAULT_MALE_ROOM_AVATAR = "/default/room1.png";
    public final static String DEFAULT_FEMALE_ROOM_AVATAR = "/default/room2.png";
    public final static String DEFAULT_ROOM_PORTRAIT = "/default/room.png";
    public final static String SHARE_URL = "https://www.baidu.com";

    public final static String WXCHATTEMP = "提交领赏申请成功，客服将按照你提交的领取方式联系您请关注公司公众号#，最新领赏通知将通过公众号推送";

    public final static String BChATROOM = "funtimefadacai";

    public final static String RONGYUN_APPKEY="mgb7ka1nmdxbg";
    public final static String RONGYUN_APPSECRET="fXnloGxdUB8aN";


    public final static String WX_APPID = "wx9c163a6bccdb1cd1";
    public final static String WX_APPSECRET = "1b9181d333b84c6c799851ae18049bb2";


    public final static String WX_GET_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token"; //获取微信TOKEN
    public final static String WX_REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token";//刷新TOKEN
    public final static String WX_CHECK_TOKEN_URL = "https://api.weixin.qq.com/sns/auth";//检查access_token有效性
    public final static String WX_USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo";//获取用户个人信息

    public final static String QQ_APPID = "101717851";
    public final static String QQ_APPSECRET = "c040a46cc00bac766ed3b1333b31fa3f";

    public final static String QQ_GET_TOKEN_URL = "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code"; //获取QQTOKEN
    public final static String QQ_REFRESH_TOKEN_URL = "https://graph.qq.com/oauth2.0/token?grant_type=refresh_token";//刷新TOKEN
    public final static String QQ_GET_OPENID_URL = "https://graph.qq.com/oauth2.0/me";//获取openId
    public final static String QQ_GET_USERINFO_URL = "https://graph.qq.com/user/get_user_info";//获取用户信息

    public final static String CONTENT_TYPE = "application/json";

    public static final String LOGIN_TEL = "TEL";
    public static final String LOGIN_WX = "WX";
    public static final String LOGIN_QQ = "QQ";
    public static final String LOGIN_ONEKEY = "ONEKEY";
    public static final String LOGIN_WEIBO = "WEIBO";


    public final static String MOBPUSH_APPID = "2d2b03f3228f1";
    public final static String MOBPUSH_APPSECRET = "e760f1673c9beb3647bda6ba4e54055f";


    public final static String AGORA_APP_ID = "392b88d30d4e406da230181043fbe58a";
    public final static String AGORA_APP_CERTIFICATE = "a4b5b901bf49490eb0b30346f6d21e95";



    public final static long TENCENT_YUN_SDK_APPID = 1400291463;
    public final static String TENCENT_YUN_SDK_APPSECRET = "c147d2655e3f472d9d9cddc0644497c992d22fc23d3c5f47d40c632f5b42d2cb";
    public final static String TENCENT_YUN_SDK_VER = "v4";
    public final static String TENCENT_YUN_SDK_HOST = "console.tim.qq.com";
    public final static String TENCENT_YUN_IDENTIFIER = "testapp";

    public final static String TENCENT_YUN_SERVICENAME_GROUP = "group_open_http_svc";
    public final static String TENCENT_YUN_SERVICENAME_IM = "im_open_login_svc";
    public final static String TENCENT_YUN_SERVICENAME_PORTRAIT = "profile";
    public final static String TENCENT_YUN_SERVICENAME_OPENIM = "openim";

    public final static String TENCENT_YUN_CREATE_GROUP = "create_group";
    public final static String TENCENT_YUN_DESTROY_GROUP = "destroy_group";
    public final static String TENCENT_YUN_ADD_GROUP_MEMBER = "add_group_member";
    public final static String TENCENT_YUN_DELETE_GROUP_MEMBER = "delete_group_member";
    public final static String TENCENT_YUN_SEND_GROUP_MSG = "send_group_msg";
    public final static String TENCENT_YUN_SEND_SYSTEM_NOTIFICATION = "send_group_system_notification";

    public final static String TENCENT_YUN_GET_GROUP_MENBER_INFO = "get_group_member_info";
    public final static String TENCENT_YUN_GET_JOINED_GROUP_LIST =  "get_joined_group_list";

    public final static String TENCENT_YUN_ACCOUNT_IMPORT = "account_import";

    public final static String TENCENT_YUN_PORTRAIT_SET = "portrait_set";
    public final static String TENCENT_YUN_BATCHSENDMSG = "batchsendmsg";

    public final static String TENCENT_YUN_QUERYSTATE = "querystate";

    public final static String TENCENT_YUN_COS_SECRETID = "AKID5Zom24pOefwypStiSu6l9G7i25qKETjm";
    public final static String TENCENT_YUN_COS_SECRETKEY = "x71qQHsFNFUS9cpcAbnjRyCF8qfLrDNQ";
    public final static String TENCENT_YUN_COS_APPID = "1300805214";
    public final static String TENCENT_YUN_COS_REGION = "ap-shanghai";
    public final static String TENCENT_YUN_COS_BUCKET = "funtime-1300805214";


    public final static int ROOM_MIC_UPPER = 1;
    public final static int ROOM_MIC_LOWER = 2;
    public final static int ROOM_MIC_STOP = 3;
    public final static int ROOM_MIC_OPEN = 4;
    public final static int ROOM_MIC_FORBID = 5;
    public final static int ROOM_MIC_RELEASE = 6;
    public final static int ROOM_CLOSE = 7;
    public final static int ROOM_GIFT_SEND = 8;
    public final static int ROOM_GIFT_SEND_ALL = 9;
    public final static int ROOM_MIC_RANDOM = 10;
    public final static int ROOM_MSG_NORMAL = 11;
    public final static int ROOM_ENTER = 12;
    public final static int ROOM_REDPACKET_SEND = 13;
    public final static int ROOM_EXPRESSION = 14;
    public final static int ROOM_MIC_HOLDING = 15;
    public final static int ROOM_KICKED = 16;
    public final static int ROOM_MANAGE = 17;
    public static final int ROOM_MANAGE_CANCEL = 18;
    public static final int ROOM_GIFT_SEND_ROOM = 19;
    public static final int ROOM_USER_COUNT = 20;
    public static final int ROOM_GIFT_SEND_ROOM_ALL = 21;
    public static final int ROOM_REDPACKET_EXIST = 22;
    public static final int SERVICE_MSG = 10001;
}
