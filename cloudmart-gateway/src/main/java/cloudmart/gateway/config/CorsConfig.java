package cloudmart.gateway.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter(){
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");    // 允许所有来源（开发阶段）
        config.addAllowedMethod("*");            // 允许所有 HTTP 方法
        config.addAllowedHeader("*");            // 允许所有请求头
        config.setAllowCredentials(true);        // 允许携带 Cookie
        config.setMaxAge(3600L);                 // 预检请求缓存 1 小时

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
