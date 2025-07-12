package com.zenika.demo.ai.agentic.agenticaidemo.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zenika.demo.ai.agentic.agenticaidemo.config.DossierRemboursementConfiguration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class DossierRemboursementSQLPrompter {
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public DossierRemboursementSQLPrompter(
        DossierRemboursementConfiguration dossierRemboursementConfiguration,
        ChatClient.Builder chatClientBuilder, ChatMemory chatMemory
    ) {
        this.chatClient = chatClientBuilder
            .defaultSystem(dossierRemboursementConfiguration.getSQLSystemPromptValue())
            .defaultOptions(OllamaOptions.builder()
                .temperature(0.1)
                .topP(0.5)
                .topK(30)
//                .numCtx(4096)
//                .numPredict(512)
                .build())
            .defaultAdvisors(new SimpleLoggerAdvisor())
            .build();
        this.chatMemory = chatMemory;
    }

    public record SQLQuery(String sql, Map<String, Object> parameters) {
    }

    private static final Pattern RESPONSE_PATTERN = Pattern.compile(
        ".*?\\s*```sql\\s*(?<sql>\\S.*?)\\s*```\\s*.*?\\s*```json\\s*(?<params>.*?)\\s*```.*",
        Pattern.DOTALL
    );

    @Cacheable("getSQL")
    public SQLQuery getSQL(String demande, String conversationId) {
        var content = chatClient.prompt(demande)
            .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(conversationId)
                .build())
            .call().content();
        var matcher = RESPONSE_PATTERN.matcher(content);
        if (matcher.find()) {
            var sql = matcher.group("sql").trim();
            var parameters = JsonParser.fromJson(matcher.group("params").trim(), new TypeReference<Map<String, Object>>() {
            });
            return new SQLQuery(sql, parameters);
        } else {
            throw new IllegalStateException("The SQL AI did not return a valid response.");
        }
    }
}
