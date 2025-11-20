package app.allstackproject.privideo.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://172.16.2.101:32019")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .exposedHeaders("Authorization", "Authorization-refresh")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
