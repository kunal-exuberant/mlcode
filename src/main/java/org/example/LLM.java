package org.example;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class LLM {
    public ChatLanguageModel getChatModel() {
        return chatModel;
    }

    private final ChatLanguageModel chatModel;

    public LLM() {
        // Prefer Groq (avoids Gemini quota); then Gemini if no Groq key
        String groqKey = EnvLoader.get("GROQ_API_KEY");
        if (groqKey == null || groqKey.isBlank()) {
            groqKey = EnvLoader.get("GROK_API_KEY");
        }
        if (groqKey != null && !groqKey.isBlank()) {
            this.chatModel = OpenAiChatModel.builder()
                    .baseUrl("https://api.groq.com/openai/v1")
                    .apiKey(groqKey)
                    .modelName("llama-3.3-70b-versatile")
                    .temperature(0.0)
                    .build();
            return;
        }
        String apiKey = EnvLoader.get("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "Set GROQ_API_KEY (or GROK_API_KEY) or GEMINI_API_KEY in .env. Groq is preferred when Gemini quota is exhausted."
            );
        }
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash-lite")
                .temperature(0.0)
                .logRequestsAndResponses(true)
                .build();
    }

    public String generate(String prompt) {
        return chatModel.generate(prompt);
    }
}
