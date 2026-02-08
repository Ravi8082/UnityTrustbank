package com.example.UnityTrustBank.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.kyc.path}")
    private String kycPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // http://localhost:1235/uploads/kyc/profile/abc.jpg -> C:/unitytrust/uploads/kyc/profile/abc.jpg
        registry.addResourceHandler("/uploads/kyc/**")
                .addResourceLocations("file:" + kycPath + "/");
    }
    
    // CORS configuration moved to SecurityConfig to avoid conflicts
}