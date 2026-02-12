package org.example.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MCP chat endpoint: send a message and get a response. Uses MCP tools when ToolCallbackProvider is available.
 * Always registered; when OPENAI_API_KEY is not set, POST /chat returns 503. See MCP_USAGE.md.
 */
@RestController
public class ChatController {

    private final ChatClient.Builder chatClientBuilder;
    private final ToolCallbackProvider toolCallbackProvider;

    public ChatController(@Autowired(required = false) ChatClient.Builder chatClientBuilder,
                          @Autowired(required = false) ToolCallbackProvider toolCallbackProvider) {
        this.chatClientBuilder = chatClientBuilder;
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @GetMapping("/chat/status")
    public Map<String, Object> status() {
        boolean chatEnabled = chatClientBuilder != null;
        return Map.of(
            "chatEnabled", chatEnabled,
            "mcpToolsAvailable", toolCallbackProvider != null,
            "hint", !chatEnabled ? "Set GROQ_API_KEY or OPENAI_API_KEY in .env for /chat." : (toolCallbackProvider == null ? "Chat works without MCP tools. To use tools, ensure MCP client connects to this server." : "Chat + MCP tools ready.")
        );
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> body) {
        if (chatClientBuilder == null) {
            return ResponseEntity.status(503).body(Map.of("error", "Chat not available. Set GROQ_API_KEY (or GROK_API_KEY) or OPENAI_API_KEY in .env."));
        }
        String message = body != null ? body.get("message") : null;
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'message' in request body"));
        }
        try {
            var chatClient = chatClientBuilder.build();
            var prompt = chatClient.prompt(message);
            if (toolCallbackProvider != null) {
                prompt = prompt.toolCallbacks(toolCallbackProvider);
            }
            String response = prompt.call().content();
            return ResponseEntity.ok(Map.of("response", response != null ? response : ""));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            boolean rateLimited = msg.contains("429") || msg.contains("rate limit") || msg.contains("Rate limit");
            if (rateLimited) {
                return ResponseEntity.status(429).body(Map.of(
                    "error", "OpenAI rate limit (429). Retry after a short wait.",
                    "detail", msg
                ));
            }
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", msg));
        }
    }
}
