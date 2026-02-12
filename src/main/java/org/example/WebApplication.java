package org.example;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Starts an HTTP server on port 8080. Run this class (not Main) to get http://localhost:8080/
 */
@SpringBootApplication(scanBasePackages = { "org.example.web", "org.example.mcp" })
public class WebApplication {

    public static void main(String[] args) {
        // #region agent log
        String userDir = System.getProperty("user.dir", ".");
        boolean envExistsCur = Files.isRegularFile(Path.of(".env"));
        boolean envExistsUserDir = Files.isRegularFile(Path.of(userDir + "/.env"));
        DebugLog.log("WebApplication.main:entry", "startup", Map.of("userDir", userDir, "envExistsCurrentDir", envExistsCur, "envExistsUserDir", envExistsUserDir), "A");
        // #endregion
        // Load .env so OPENAI_API_KEY is available to Spring (try current dir then project root)
        EnvLoader.load(".env");
        // #region agent log
        String keyAfterFirst = EnvLoader.get("OPENAI_API_KEY");
        DebugLog.log("WebApplication.main:afterFirstLoad", "after load .env", Map.of("openaiKeyPresent", keyAfterFirst != null && !keyAfterFirst.isBlank(), "keyLen", keyAfterFirst != null ? keyAfterFirst.length() : 0), "A");
        // #endregion
        if (keyAfterFirst == null || keyAfterFirst.isBlank()) {
            EnvLoader.load(userDir + "/.env");
        }
        String groqKey = EnvLoader.get("GROQ_API_KEY");
        if (groqKey == null || groqKey.isBlank()) {
            groqKey = EnvLoader.get("GROK_API_KEY");
        }
        String openaiKey = EnvLoader.get("OPENAI_API_KEY");
        // #region agent log
        DebugLog.log("WebApplication.main:beforeSetProperty", "before setProperty", Map.of("openaiKeyPresent", openaiKey != null && !openaiKey.isBlank(), "keyLen", openaiKey != null ? openaiKey.length() : 0), "B");
        // #endregion
        SpringApplication app = new SpringApplication(WebApplication.class);
        if (groqKey != null && !groqKey.isBlank()) {
            // Groq (OpenAI-compatible API at api.groq.com) as default LLM
            System.setProperty("spring.ai.openai.api-key", groqKey);
            System.setProperty("spring.ai.openai.base-url", "https://api.groq.com/openai");
            System.setProperty("spring.ai.openai.chat.options.model", "llama-3.3-70b-versatile");
            app.setDefaultProperties(Map.of(
                "spring.ai.openai.api-key", groqKey,
                "spring.ai.openai.base-url", "https://api.groq.com/openai",
                "spring.ai.openai.chat.options.model", "llama-3.3-70b-versatile"
            ));
            // #region agent log
            DebugLog.log("WebApplication.main:afterSetProperty", "Groq defaultProperties set", Map.of("set", true), "B");
            // #endregion
            System.out.println("[WebApplication] Groq (llama-3.3-70b-versatile) loaded from .env (GROQ_API_KEY or GROK_API_KEY).");
        } else if (openaiKey != null && !openaiKey.isBlank()) {
            System.setProperty("OPENAI_API_KEY", openaiKey);
            System.setProperty("spring.ai.openai.api-key", openaiKey);
            app.setDefaultProperties(Map.of("spring.ai.openai.api-key", openaiKey));
            // #region agent log
            DebugLog.log("WebApplication.main:afterSetProperty", "system properties and defaultProperties set", Map.of("set", true), "B");
            // #endregion
            System.out.println("[WebApplication] OPENAI_API_KEY loaded from .env (length=" + openaiKey.length() + ")");
        } else {
            System.out.println("[WebApplication] No chat API key in .env. Set GROQ_API_KEY (or GROK_API_KEY) for Groq, or OPENAI_API_KEY for OpenAI – /chat will not be available.");
        }
        app.run(args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) {
        // #region agent log
        var ctx = event.getApplicationContext();
        String[] chatClientBeans = ctx.getBeanNamesForType(ChatClient.class);
        String[] builderBeans = ctx.getBeanNamesForType(ChatClient.Builder.class);
        boolean hasChatController = false;
        try {
            hasChatController = ctx.getBean("chatController") != null;
        } catch (Throwable t) {
            hasChatController = false;
        }
        String springAiKey = System.getProperty("spring.ai.openai.api-key");
        String envResolved = ctx.getEnvironment().getProperty("spring.ai.openai.api-key");
        DebugLog.log("WebApplication.onReady", "context ready", Map.of("chatClientBeanCount", chatClientBeans.length, "chatClientBuilderBeanCount", builderBeans.length, "hasChatController", hasChatController, "springAiKeySet", springAiKey != null && !springAiKey.isBlank(), "envResolvedKeyLen", envResolved != null ? envResolved.length() : 0), "C");
        DebugLog.log("WebApplication.onReady", "controller registration", Map.of("chatControllerRegistered", hasChatController), "D");
        // #endregion
        System.out.println("\n>>> Server is up. Open: http://localhost:8080/  |  POST /chat  |  GET /chat/status\n");
    }
}
