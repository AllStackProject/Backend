package app.allstackproject.privideo.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiConfig {

    /**
     * WebClient for Rtzr STT API
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    /**
     * ChatClient for Gemini AI
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
