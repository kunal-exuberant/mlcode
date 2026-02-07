package org.example;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

public class LLM {
    private final ChatLanguageModel chatModel;

    public LLM() {
        String apiKey = EnvLoader.get("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "GEMINI_API_KEY is not set. Add it to a .env file or set the environment variable."
            );
        }
        // gemini-2.5-flash-lite has free-tier quota (15 RPM, 1000 RPD); gemini-2.0-flash often has limit 0 on free
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash-lite")
                .build();
    }

    public String generate(String prompt) {
        return chatModel.generate(prompt);
    }
}
