package org.example.mcp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPClient {

    @Bean
    @ConditionalOnBean(ChatClient.Builder.class)
    public CommandLineRunner demo(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpTools) {
        return args -> {
            String response = chatClientBuilder.build()
                    .prompt("What's the weather like in Paris?")
                    .toolCallbacks(mcpTools)
                    .call()
                    .content();
            System.out.println(response);
        };
    }
}
