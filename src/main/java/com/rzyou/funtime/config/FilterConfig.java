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
        registrationBean.setFilter(securityFilter());
        List<String> urlPatterns = new ArrayList<>();
        String patten = "/*";
        urlPatterns.add(patten);
        registrationBean.setUrlPatterns(urlPatterns);
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public TokenAuthorFilter securityFilter() {
        return new TokenAuthorFilter();
    }


}
