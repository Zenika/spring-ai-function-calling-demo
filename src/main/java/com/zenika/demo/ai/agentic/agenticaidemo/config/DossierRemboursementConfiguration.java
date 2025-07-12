package com.zenika.demo.ai.agentic.agenticaidemo.config;

import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

@ConfigurationProperties(prefix = "dossier-remboursement")
public record DossierRemboursementConfiguration(Resource sqlSystemPrompt, Resource systemPrompt) {

    @SneakyThrows
    public String getSQLSystemPromptValue() {
        return sqlSystemPrompt.getContentAsString(StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public String getSystemPromptValue() {
        return systemPrompt.getContentAsString(StandardCharsets.UTF_8);
    }
}
