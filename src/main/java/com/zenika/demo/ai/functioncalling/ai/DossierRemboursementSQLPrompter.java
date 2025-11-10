package com.zenika.demo.ai.functioncalling.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zenika.demo.ai.functioncalling.config.DossierRemboursementConfiguration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class DossierRemboursementSQLPrompter {
    private final ChatClient chatClient;

    public DossierRemboursementSQLPrompter(
        DossierRemboursementConfiguration dossierRemboursementConfiguration,
        ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
            .defaultSystem(dossierRemboursementConfiguration.getSQLSystemPromptValue())
            .defaultOptions(OllamaOptions.builder()
                .temperature(0.1)
                .topP(0.5)
                .topK(30)
                .build())
            .defaultAdvisors(new SimpleLoggerAdvisor())
            .build();
    }

    public record SQLQuery(String sql, Map<String, Object> parameters) {
    }

    private static final Pattern RESPONSE_PATTERN = Pattern.compile(
        ".*?\\s*```sql\\s*(?<sql>\\S.*?)\\s*```\\s*.*?\\s*```json\\s*(?<params>.*?)\\s*```.*",
        Pattern.DOTALL
    );

    public SQLQuery getSQL(String demande) {
        if (demande == null || demande.isBlank()) {
            throw new IllegalArgumentException("Cannot process null or empty request.");
        }
        var content = chatClient.prompt(demande)
            .call().content();
        if (content == null) {
            throw new IllegalStateException("The SQL AI did not return any content.");
        }
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
