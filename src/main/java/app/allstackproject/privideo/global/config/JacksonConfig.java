package app.allstackproject.privideo.global.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter LDT_NO_NANOS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            JavaTimeModule module = new JavaTimeModule();
            module.addSerializer(java.time.LocalDateTime.class, new LocalDateTimeSerializer(LDT_NO_NANOS));
            module.addDeserializer(java.time.LocalDateTime.class, new LocalDateTimeDeserializer(LDT_NO_NANOS));
            builder.modules(module);

            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            builder.featuresToDisable(
                    MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,
                    SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS
            );
        };
    }
}
