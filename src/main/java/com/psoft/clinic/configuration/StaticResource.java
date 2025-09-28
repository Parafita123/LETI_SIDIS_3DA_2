package com.psoft.clinic.configuration;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResource implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/physician/**")
        .addResourceLocations("file:"
                + System.getProperty("user.dir")
                + "/src/main/resources/physician/")
                .setCachePeriod(3600);
    }
}
