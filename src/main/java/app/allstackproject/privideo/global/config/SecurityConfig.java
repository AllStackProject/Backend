package app.allstackproject.privideo.global.config;

import app.allstackproject.privideo.global.exception.handler.CustomAccessDeniedHandler;
import app.allstackproject.privideo.global.exception.handler.CustomAuthEntryPoint;
import app.allstackproject.privideo.domain.organization.repository.OrgRedisRepository;
import app.allstackproject.privideo.global.security.JwtAuthFilter;
import app.allstackproject.privideo.global.security.JwtProvider;
import app.allstackproject.privideo.global.security.JwtSecurityProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final OrgRedisRepository orgRedisRepository;
    private final JwtSecurityProperties jwtSecurityProperties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] permitPatterns = buildPermitPatterns();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers((headerConfig) ->
                        headerConfig.frameOptions(FrameOptionsConfig::disable
                        )
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permitPatterns)
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .addFilterAfter(new JwtAuthFilter(jwtProvider, orgRedisRepository), ExceptionTranslationFilter.class)
                .cors(cors -> {
                })
                .formLogin(f -> f.disable())
                .httpBasic(b -> b.disable());
        ;

        return http.build();
    }

    private String[] buildPermitPatterns() {
        List<String> staticPatterns = List.of(
                "/error", "/favicon.ico",
                "/h2-console/**", "/v3/api-docs/**",
                "/swagger-ui.html", "/swagger-ui/**"
        );

        List<String> combined = new ArrayList<>(staticPatterns);

        List<String> excludedPatterns = jwtSecurityProperties.getExcludedPatterns();

        if (excludedPatterns != null && !excludedPatterns.isEmpty()) {
            combined.addAll(excludedPatterns);
        }

        return combined.toArray(new String[0]);
    }
}
