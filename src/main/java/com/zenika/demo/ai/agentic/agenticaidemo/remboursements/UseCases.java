package com.zenika.demo.ai.agentic.agenticaidemo.remboursements;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class UseCases {

    private final DossiersRemboursements dossiersRemboursements;

    public DossierRemboursement cloturerDossier(DossierRemboursement.Id id, @Nullable String commentaire) {
        log.info("Valider dossier {}", id);
        return appliquer(id, dossier -> dossier.cloturer(commentaire));
    }

    public DossierRemboursement rejeterDossier(DossierRemboursement.Id id, @Nullable String commentaire) {
        log.info("Invalider dossier {}", id);
        return appliquer(id, dossier -> dossier.rejeter(commentaire));
    }

    private DossierRemboursement appliquer(DossierRemboursement.Id id, Function<DossierRemboursement, DossierRemboursement> mutation) {
        return dossiersRemboursements.findById(id)
            .map(mutation::apply)
            .map(dossierModifie -> {
                dossiersRemboursements.save(dossierModifie);
                return dossierModifie;
            }).orElse(null);
    }
}
