package com.zenika.demo.ai.functioncalling.ai;

import com.zenika.demo.ai.functioncalling.config.DossierRemboursementConfiguration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DossierRemboursementPrompter {

    private final String systemPrompt;
    private final ChatClient chatClient;

    private final DossierRemboursementTools dossierRemboursementTools;

    private final ChatMemory chatMemory;

    public DossierRemboursementPrompter(
        ChatClient.Builder chatClientBuilder,
        DossierRemboursementConfiguration dossierRemboursementConfiguration,
        DossierRemboursementTools dossierRemboursementTools,
        ChatMemory chatMemory) {
        this.systemPrompt = dossierRemboursementConfiguration.getSystemPromptValue();
        this.chatClient = chatClientBuilder
            .defaultAdvisors(new SimpleLoggerAdvisor())
            .defaultOptions(OllamaOptions.builder()
                .temperature(0.1)
                .build())
            .build();
        this.dossierRemboursementTools = dossierRemboursementTools;
        this.chatMemory = chatMemory;
    }


    /**
     * Traite une demande de remboursement en récupérant les informations du dossier associé.
     *
     * @param demande        la demande utilisateur
     * @param conversationId l'identifiant de la conversation pour la mémoire de chat
     * @return la réponse de l'agent d'assurance concernant le dossier
     */
    public Flux<String> traiterDemandeAsync(String demande, String conversationId) {
        AtomicLong startTime = new AtomicLong(System.nanoTime());

        return getPrompt(demande, conversationId).stream().content()
            .concatWith(Flux.defer(() -> {
                var endTime = System.nanoTime() * 1.0;
                var durationSec = (endTime - startTime.get()) / 1_000_000_000;
                return Flux.just("\n", "\n", "Temps de traitement : " + durationSec + " sec");
            }));
    }

    /**
     * Traite une demande de remboursement de manière synchrone.
     *
     * @param demande        la demande utilisateur
     * @param conversationId l'identifiant de la conversation pour la mémoire de chat
     * @return la réponse de l'agent d'assurance concernant le dossier
     */
    public String traiterDemande(String demande, String conversationId) {
        return getPrompt(demande, conversationId).call().content();
    }

    private ChatClient.ChatClientRequestSpec getPrompt(String demande, String conversationId) {
        return chatClient
            .prompt(demande)
            .system(systemPrompt)
            .tools(dossierRemboursementTools)
            .advisors(
                MessageChatMemoryAdvisor.builder(chatMemory)
                    .conversationId(conversationId)
                    .build()
            );
    }
}