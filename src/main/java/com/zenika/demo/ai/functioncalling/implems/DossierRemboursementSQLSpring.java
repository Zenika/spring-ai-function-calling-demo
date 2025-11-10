package com.zenika.demo.ai.functioncalling.implems;

import com.zenika.demo.ai.functioncalling.remboursements.DossierRemboursement;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DossierRemboursementSQLSpring extends CrudRepository<DossierRemboursementSQLSpring.DossierRemboursementSQL, Long> {

    @Table("dossier_remboursement")
    record DossierRemboursementSQL(@Id Long id, LocalDate dateSoin, String justificatifs,
                                   DossierRemboursement.StatutDemande statut, String nom,
                                   int numeroAssure, DossierRemboursement.TypeSoin typeSoin, BigDecimal montant,
                                   LocalDate dateDemande, LocalDate dateDecision,
                                   String commentaire, DossierRemboursement.Decision decision) {
    }
}
