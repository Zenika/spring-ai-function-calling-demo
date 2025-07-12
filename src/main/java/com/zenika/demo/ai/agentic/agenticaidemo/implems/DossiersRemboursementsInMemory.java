package com.zenika.demo.ai.agentic.agenticaidemo.implems;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenika.demo.ai.agentic.agenticaidemo.remboursements.DossierRemboursement;
import com.zenika.demo.ai.agentic.agenticaidemo.remboursements.DossiersRemboursements;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
//@Primary
@Slf4j
public class DossiersRemboursementsInMemory implements DossiersRemboursements {

    private final Map<DossierRemboursement.Id, DossierRemboursement> dossierRemboursements;

    private final ObjectMapper objectMapper;

    public DossiersRemboursementsInMemory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.dossierRemboursements = dossierRemboursements();
    }

    @SneakyThrows
    private Map<DossierRemboursement.Id, DossierRemboursement> dossierRemboursements() {
        URL resource = getClass().getResource("/DemandesRemboursements.json");
        return objectMapper.readValue(resource, new TypeReference<List<DossierRemboursement>>() {
            })
            .stream()
            .collect(Collectors.toMap(
                DossierRemboursement::id,
                Function.identity()
            ));
    }

    @Override
    public void save(DossierRemboursement dossier) {
        dossierRemboursements.put(dossier.id(), dossier);
    }

    @Override
    public Optional<DossierRemboursement> findById(DossierRemboursement.Id id) {
        log.info("findById {}", id);
        return Optional.ofNullable(dossierRemboursements.get(id));
    }

    @Override
    public QueryResult findByNaturalLanguageQuery(String query, String conversationId) {
        throw new UnsupportedOperationException("In memory repository does not support queries by natural language.");
    }
}
