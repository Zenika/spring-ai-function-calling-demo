package com.zenika.demo.ai.functioncalling.remboursements;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @param justificatifs Liste des justificatifs sous forme de chaîne
 * @param statut        Statut du dossier (incomplet, complet, suspect)
 * @param nom           Nom de l'assuré
 * @param numeroAssure  Numéro de l'assuré
 * @param typeSoin      Type de soin (consultation, hospitalisation, etc.)
 * @param montant       Montant du remboursement demandé
 * @param dateDemande   Date de la demande de remboursement
 * @param dateDecision  Date de la décision finale
 * @param commentaire   Commentaire de l'agent sur le dossier
 * @param decision      Décision finale (validée, rejetée, en attente)
 */
@Builder(toBuilder = true, access = AccessLevel.PRIVATE)
public record DossierRemboursement(Id id, LocalDate dateSoin, String justificatifs, StatutDemande statut, String nom,
                                   int numeroAssure, TypeSoin typeSoin, BigDecimal montant, LocalDate dateDemande,
                                   LocalDate dateDecision, String commentaire, Decision decision) {

    public DossierRemboursement cloturer(@Nullable String commentaire) {
        return traiterDossier(commentaire, Decision.ACCEPTE);
    }

    public DossierRemboursement rejeter(@Nullable String commentaire) {
        return traiterDossier(commentaire, Decision.REJETTE);
    }

    private DossierRemboursement traiterDossier(String commentaire, Decision decision) {
        if (this.decision == decision) {
            return this;
        }
        var now = LocalDate.now();
        return this.toBuilder()
            .decision(decision)
            .dateDecision(now)
            .commentaire(updatedCommentaire(commentaire, now))
            .build();
    }

    private String updatedCommentaire(@Nonnull String commentaire, LocalDate now) {
        var newCommentaire = now.toString() + " : " + commentaire;
        return "%s%n%s".formatted(this.commentaire, newCommentaire);
    }

    public enum StatutDemande {
        INCOMPLET, COMPLET, SUSPECT
    }

    public enum TypeSoin {
        CONSULTATION, HOSPITALISATION, ANALYSE, RADIOLOGIE, AUTRE
    }

    public enum Decision {
        ACCEPTE, REJETTE, EN_ATTENTE
    }

    public record Id(Long value) implements Comparable<Id> {
        public static Id of(Long value) {
            return new Id(value);
        }

        @Override
        public int compareTo(Id o) {
            return value.compareTo(o.value);
        }
    }
}
