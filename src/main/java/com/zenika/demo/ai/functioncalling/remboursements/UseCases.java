package com.zenika.demo.ai.functioncalling.remboursements;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class UseCases {

    private final DossiersRemboursements dossiersRemboursements;


    public enum TypeMiseAJour {
        CLOTURE, REJET
    }
    public DossierRemboursement traiterDossier(DossierRemboursement.Id id, TypeMiseAJour typeMiseAJour) {
        log.info("Traiter dossier {}", id);
        return switch (typeMiseAJour) {
            case CLOTURE -> appliquer(id, dossier -> dossier.cloturer("Dossier cloturé par l'agent via l'outil Agentic"));
            case REJET -> appliquer(id, dossier -> dossier.rejeter("Dossier rejeté par l'agent via l'outil Agentic"));
        };
    }

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
