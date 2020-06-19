package com.rzyou.funtime.websocket;
import com.rzyou.funtime.service.RoomService;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.yeauty.annotation.*;
import org.yeauty.pojo.Session;

import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

//@ServerEndpoint(path = "/ws/app")
@Slf4j
public class AppWebSocket {

    /**静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。*/
    private static int onlineCount = 0;
    /**concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。*/
    private static ConcurrentHashMap<String,AppWebSocket> webSocketMap = new ConcurrentHashMap<>();
    /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
    private Session session;
    /**接收userId*/
    private String userId="";

    private Timer timer = new Timer("game21 timer");


    @BeforeHandshake
    public void handshake(Session session, HttpHeaders headers, @RequestParam String token, @RequestParam MultiValueMap reqMap, @PathVariable Map pathMap){
        session.setSubprotocols("stomp");
        if (StringUtils.isBlank(token)){
            log.info("Authentication failed!");
            session.close();
        }
    }

    @OnOpen
    public void onOpen(Session session, HttpHeaders headers, @RequestParam String token, @RequestParam MultiValueMap reqMap, @PathVariable Map pathMap){
        log.info("有新窗口开始监听:"+token);
        this.session = session;
        this.userId=token;
        webSocketMap.put(userId,this);
        try {
            sendMessage("发牌开始");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        sendMessage("");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            },1000*60*18);
        } catch (IOException e) {
            log.error("websocket IO异常");
        }

    }

    @OnClose
    public void onClose(Session session) throws IOException {
        if(webSocketMap.get(this.userId)!=null){
            webSocketMap.remove(this.userId);
            log.info("有一连接关闭");
        }

    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("发生错误");
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        log.info("收到来自窗口"+userId+"的信息:"+message);

    }

    @OnBinary
    public void onBinary(Session session, byte[] bytes) {
        for (byte b : bytes) {
            System.out.println(b);
        }
        session.sendBinary(bytes);
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.sendText(message);
    }
    public static void sendInfo(String message, List<String> userIds) throws IOException {
        log.info("推送内容:"+message);
        AppWebSocket appWebSocket;
        for (String userId : userIds){
            appWebSocket = webSocketMap.get(userId);
            appWebSocket.sendMessage(message);
        }

    }


    @OnEvent
    public void onEvent(Session session, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    System.out.println("read idle");
                    break;
                case WRITER_IDLE:
                    System.out.println("write idle");
                    break;
                case ALL_IDLE:
                    System.out.println("all idle");
                    break;
                default:
                    break;
            }
        }
    }
}
