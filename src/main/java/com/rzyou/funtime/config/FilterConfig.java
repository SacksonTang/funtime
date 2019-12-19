package com.rzyou.funtime.config;

import com.rzyou.funtime.filter.TokenAuthorFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        TokenAuthorFilter tokenAuthorFilter = new TokenAuthorFilter();
        registrationBean.setFilter(tokenAuthorFilter);
        List<String> urlPatterns = new ArrayList<>();
        String patten = "/*";
        urlPatterns.add(patten);
        registrationBean.setUrlPatterns(urlPatterns);
        registrationBean.setOrder(1);
        return registrationBean;
    }


}
