package app.allstackproject.privideo.common.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "secret.jwt")
public class JwtSecurityProperties {

    private List<String> excludedPatterns = new ArrayList<>();
}