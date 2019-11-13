package com.rzyou.funtime.filter;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.common.request.RequestReaderHttpServletRequestWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class TokenAuthorFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(TokenAuthorFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        ServletRequest requestWrapper ;
        if(request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest)request;
            String uri = req.getRequestURI();
            if(!uri.startsWith("/login")&&uri.startsWith("/pay/notifyWxPay")){
                requestWrapper =  request;
                String str = HttpHelper.getBodyString(requestWrapper);
                log.info("请求Body: {} ", str);
                JSONObject obj = JSONObject.parseObject(str);
                String token = obj.getString("token");
                if(StringUtils.isBlank(token)){
                    returnJson((HttpServletResponse) response);
                }else {
                    filterChain.doFilter(request, response);
                }
            }else{
                filterChain.doFilter(request,response);
            }
        }


    }

    @Override
    public void destroy() {

    }

    private void returnJson(HttpServletResponse response){
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try {
            writer = response.getWriter();
            ResultMsg<Object> resultMsg = new ResultMsg<>(ErrorMsgEnum.USER_TOKEN_ERROR.getValue(),ErrorMsgEnum.USER_TOKEN_ERROR.getDesc());
            writer.print(JSONObject.toJSON(resultMsg));
        } catch (Exception e){
            log.error("拦截器输出流异常"+e);
        } finally {
            if(writer != null){
                writer.close();
            }
        }
    }


}
