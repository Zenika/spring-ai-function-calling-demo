package com.zenika.demo.ai.functioncalling.restapi;

import com.zenika.demo.ai.functioncalling.ai.DossierRemboursementPrompter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ConversationController {
    private final DossierRemboursementPrompter dossierRemboursementPrompterService;

    @PostMapping("/conversationsAsync")
    public Flux<String> updateConversationAsync(@RequestBody ConversationPayload payload) {
        return dossierRemboursementPrompterService.traiterDemandeAsync(payload.message, payload.conversationId);
    }

    @PostMapping("/conversations")
    public String updateConversation(@RequestBody ConversationPayload payload) {
        return dossierRemboursementPrompterService.traiterDemande(payload.message, payload.conversationId);
    }

    public record ConversationPayload(String conversationId, String message) {}
}
