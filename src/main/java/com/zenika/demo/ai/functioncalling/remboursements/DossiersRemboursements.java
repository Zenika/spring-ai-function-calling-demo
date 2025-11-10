package com.zenika.demo.ai.functioncalling.remboursements;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DossiersRemboursements {
    /**
     * Enregistre un dossier de remboursement.
     *
     * @param dossier le dossier à enregistrer
     */
    void save(DossierRemboursement dossier);

    /**
     * Récupère un dossier de remboursement par son identifiant.
     *
     * @param id l'identifiant du dossier
     * @return le dossier correspondant, ou null s'il n'existe pas
     */
    Optional<DossierRemboursement> findById(DossierRemboursement.Id id);

    record QueryResult(List<String> columnNames, List<? extends Map<String, Object>> rows) {
    }

    /**
     * Récupère tous les dossiers de remboursement à partir d'une requête en langage naturel.
     *
     * @param query          la requête en langage naturel
     * @return la liste des dossiers correspondants
     */
    QueryResult findByNaturalLanguageQuery(String query);
}
