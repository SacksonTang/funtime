package com.rzyou.funtime.utils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    /**
     * @Author：
     * @Description：获取某个目录下所有直接下级文件，不包括目录下的子目录的下的文件，所以不用递归获取
     * @Date：
     */
    public static List<String> getFiles(String path) {
        List<String> files = new ArrayList<>();
        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                files.add(tempList[i].getName());
                //文件名，不包含路径
                //String fileName = tempList[i].getName();
            }
            if (tempList[i].isDirectory()) {
                //这里就不递归了，
            }
        }
        return files;
    }

    private static URL url;
    private static HttpURLConnection con;
    private static int state = -1;
    /**
     * 功能：检测当前URL是否可连接或是否有效,
     * 描述：最多连接网络 5 次, 如果 5 次都不成功，视为该地址不可用
     * @param urlStr 指定URL网络地址
     * @return URL
     */
    public static synchronized boolean isConnect(String urlStr) {
        int counts = 0;
        if (urlStr == null || urlStr.length() <= 0) {
            return false;
        }
        boolean bool = false;
        while (counts < 5) {
            try {
                url = new URL(urlStr);
                con = (HttpURLConnection) url.openConnection();
                state = con.getResponseCode();
                System.out.println(counts +"="+state);
                if (state == 200){
                    bool = true;
                }
                break;
            }catch (Exception ex) {
                counts++;
                urlStr = null;
                continue;
            }
        }
        return bool;
    }


    public static void main(String[] args) {
        isConnect("https://music-1300805214.cos.ap-shanghai.myqcloud.com/music/2020热门网络歌曲/醉红尘 - 魏新雨.mp");
    }

}
