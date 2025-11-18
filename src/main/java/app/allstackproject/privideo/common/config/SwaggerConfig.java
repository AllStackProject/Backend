package app.allstackproject.privideo.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final String BOOTSTRAP_AUTH_KEY = "bootstrapAuth";
    public static final String ORG_AUTH_KEY = "orgAuth";

    @Bean
    public OpenAPI openAPI() {
        Components components = new Components()
                .addSecuritySchemes(BOOTSTRAP_AUTH_KEY, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .name("Authorization")
                        .description("Bootstrap JWT (로그인/마이페이지 등 사용자 단위)"))
                .addSecuritySchemes(ORG_AUTH_KEY, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .name("Authorization")
                        .description("Organization-scoped JWT (조직 내부 API)"));

        Info info = new Info()
                .title("Privideo API")
                .description("간편하게 공유하는 “우리”만의 영상 공간 [Privideo] API");

        return new OpenAPI()
                .components(components)
                .info(info);
    }

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return new ModelResolver(objectMapper);
    }
}
