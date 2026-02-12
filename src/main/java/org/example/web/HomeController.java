package org.example.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
            "message", "RAG Chat API is running",
            "cli", "Run org.example.Main for the console RAG chat.",
            "health", "ok"
        );
    }

    @GetMapping("/mcp-info")
    public Map<String, Object> mcpInfo() {
        return Map.of(
            "mcpServer", "This app exposes MCP tools at http://localhost:8080 (SSE: /sse, messages: /mcp/message)",
            "mcpClient", "POST /chat with {\"message\": \"your question\"} to use the LLM with MCP tools (e.g. weather). Requires OpenAI API key; see MCP_USAGE.md.",
            "docs", "See MCP_USAGE.md in the project root for full steps."
        );
    }
}
