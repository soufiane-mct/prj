package com.aliboo.book.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

@Slf4j
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        log.debug("Configuring CORS mappings");
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        log.debug("Configuring resource handlers");
        
        // Set a low order to ensure this handler is checked last
        registry.setOrder(Ordered.LOWEST_PRECEDENCE);
        
        // Disable default servlet handling of static resources
        registry.setOrder(1);
        
        // Explicitly map static resources
        registry.addResourceHandler(
            "/static/**",
            "/assets/**"
        ).addResourceLocations("classpath:/static/");
        
        // Serve specific static files directly
        registry.addResourceHandler(
            "/*.js",
            "/*.css",
            "/*.html",
            "/*.ico",
            "/*.png",
            "/*.jpg",
            "/*.jpeg",
            "/*.gif",
            "/*.svg",
            "/*.woff",
            "/*.woff2",
            "/*.ttf"
        ).addResourceLocations("classpath:/static/");
        
        // Serve index.html for Angular routes
        registry.addResourceHandler(
            "/",
            "/book/**",
            "/login",
            "/register",
            "/profile"
        ).addResourceLocations("classpath:/static/index.html");
    }
    
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
