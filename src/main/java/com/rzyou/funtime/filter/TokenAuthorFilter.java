package com.rzyou.funtime.filter;

import com.alibaba.fastjson.JSON;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.jwt.util.JwtHelper;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@Slf4j
public class TokenAuthorFilter implements Filter {

    @Autowired
    UserService userService;

    @Override
    public void init(FilterConfig filterConfig)  {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rep = (HttpServletResponse) response;

        //设置允许跨域的配置
        // 这里填写你允许进行跨域的主机ip（正式上线时可以动态配置具体允许的域名和IP）
        rep.setHeader("Access-Control-Allow-Origin", "*");
        // 允许的访问方法
        rep.setHeader("Access-Control-Allow-Methods","POST, GET, PUT, OPTIONS, DELETE, PATCH");
        // Access-Control-Max-Age 用于 CORS 相关配置的缓存
        rep.setHeader("Access-Control-Max-Age", "3600");
        rep.setHeader("Access-Control-Allow-Headers","token,Origin, X-Requested-With, Content-Type, Accept");


        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");

        String uri = req.getRequestURI();
        log.info("访问URI!===========uri=========> {}",uri);
        if (uri.startsWith("/login")||uri.startsWith("/druid")||uri.startsWith("/callback")){
            filterChain.doFilter(request, response);
            return;
        }

        String token = req.getHeader("token");//header方式
        ResultMsg<Object> resultInfo = new ResultMsg<>();
        boolean isFilter = false;


        String method = ((HttpServletRequest) request).getMethod();
        if (method.equals("OPTIONS")) {
            rep.setStatus(HttpServletResponse.SC_OK);
        }else{


            if (null == token || token.isEmpty()) {
                resultInfo.setCode(ErrorMsgEnum.USER_TOKEN_EMPTY.getValue());
                resultInfo.setMsg(ErrorMsgEnum.USER_TOKEN_EMPTY.getDesc());
            } else {
                try {
                    Map<String, Object> map = JwtHelper.validateLogin(token);
                    if (map.get("userId")==null){
                        resultInfo.setCode(ErrorMsgEnum.USER_NOT_EXISTS.getValue());
                        resultInfo.setMsg(ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
                    }else {

                        FuntimeUser user = userService.queryUserById(Long.parseLong(map.get("userId").toString()));
                        if (user==null){
                            resultInfo.setCode(ErrorMsgEnum.USER_NOT_EXISTS.getValue());
                            resultInfo.setMsg(ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
                        }else {
                            if (user.getState() == 2){
                                resultInfo.setCode(ErrorMsgEnum.USER_IS_DELETE.getValue());
                                resultInfo.setMsg(ErrorMsgEnum.USER_IS_DELETE.getDesc());
                            }else {
                                String uuid = map.get("nonceStr").toString();
                                if (!user.getToken().equals(uuid)){
                                    resultInfo.setCode(ErrorMsgEnum.USER_TOKEN_ERROR.getValue());
                                    resultInfo.setMsg(ErrorMsgEnum.USER_TOKEN_ERROR.getDesc());
                                }else {
                                    HttpHelper.setUserId(user.getId());
                                    isFilter = true;
                                }
                            }
                        }
                    }
                }catch (BusinessException e){
                    resultInfo.setCode(e.getCode());
                    resultInfo.setMsg(e.getMsg());
                }catch (Exception e){
                    resultInfo.setCode(ErrorMsgEnum.USER_TOKEN_ERROR.getValue());
                    resultInfo.setMsg(ErrorMsgEnum.USER_TOKEN_ERROR.getDesc());
                }

            }


            if (resultInfo.getCode() != ErrorMsgEnum.SUCCESS.getValue()) {// 验证失败
                PrintWriter writer = null;
                OutputStreamWriter osw = null;
                try {
                    osw = new OutputStreamWriter(response.getOutputStream(),
                            "UTF-8");
                    writer = new PrintWriter(osw, true);
                    String jsonStr = JSON.toJSONString(resultInfo);
                    writer.write(jsonStr);
                    writer.flush();
                    writer.close();
                    osw.close();
                } catch (UnsupportedEncodingException e) {
                    log.error("过滤器返回信息失败:" + e.getMessage(), e);
                } catch (IOException e) {
                    log.error("过滤器返回信息失败:" + e.getMessage(), e);
                } finally {
                    if (null != writer) {
                        writer.close();
                    }
                    if (null != osw) {
                        osw.close();
                    }
                }
                return;
            }

            if (isFilter) {

                filterChain.doFilter(request, response);
            }
        }
    }

    @Override
    public void destroy() {

    }
}
