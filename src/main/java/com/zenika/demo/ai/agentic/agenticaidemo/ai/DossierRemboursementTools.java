package com.zenika.demo.ai.agentic.agenticaidemo.ai;

import com.zenika.demo.ai.agentic.agenticaidemo.remboursements.DossierRemboursement;
import com.zenika.demo.ai.agentic.agenticaidemo.remboursements.DossiersRemboursements;
import com.zenika.demo.ai.agentic.agenticaidemo.remboursements.DossiersRemboursements.QueryResult;
import com.zenika.demo.ai.agentic.agenticaidemo.remboursements.UseCases;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.execution.ToolCallResultConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class DossierRemboursementTools {

    private final DossiersRemboursements dossiersRemboursements;
    private final UseCases useCases;
    private final ChatMemory chatMemory;

    public DossierRemboursementTools(DossiersRemboursements dossiersRemboursements, UseCases useCases, ChatMemory chatMemory) {
        this.dossiersRemboursements = dossiersRemboursements;
        this.useCases = useCases;
        this.chatMemory = chatMemory;
    }

    @Tool(
        name = "internalActionGetDossier",
        description = """
            uniquement pour usage interne
            """
//        resultConverter = DossierRemboursementToMarkdownConverter.class
    )
    public DossierRemboursement getDossier(
        @ToolParam(description = "id (synonymes: identifiant, numéro, n°, no) du dossier")
        Long dossierId) {
        return dossiersRemboursements.findById(buildId(dossierId)).orElse(null);
    }

    @Tool(
        name = "internalActionCloturerDossier",
        description = """
            uniquement pour usage interne
            """
    )
    public DossierRemboursement cloturerDossier(
        @ToolParam(description = "identifiant du dossier à cloturer") Long dossierId,
        @ToolParam(description = "contexte de l'outil") ToolContext contexte) {
        var dossierRemboursement = useCases.cloturerDossier(buildId(dossierId), "Dossier cloturé par l'agent via l'outil Agentic");
        clearChatMemory(contexte);
        return dossierRemboursement;
    }

    @Tool(
        name = "internalActionRejeterDossier",
        description = """
            uniquement pour usage interne
            """
//        description = """
//            Rejeter (synonymes: invalider, refuser) un dossier de remboursement.
//            Le dossier sera marqué comme REFUSE
//            """
    )
    public DossierRemboursement rejeterDossier(
        @ToolParam(description = "identifiant du dossier à invalider") Long dossierId) {
        var dossierRemboursement = useCases.rejeterDossier(buildId(dossierId), "Dossier rejeté par l'agent via l'outil Agentic");
//        clearChatMemory(conversationId);
        return dossierRemboursement;
    }

    private void clearChatMemory(ToolContext contexte) {
        log.info("contexte de l'outil: {}", contexte.getContext());
        log.info("historique du contexte de l'outil");
        contexte.getToolCallHistory().forEach(message -> log.info("Message dans l'historique de l'outil: {}", message));
        var conversationId = getConversationId(contexte);
        if (conversationId != null) {
            chatMemory.get(conversationId).forEach(
                message -> log.info("Message dans la mémoire de chat: {}", message)
            );
            log.info("Nettoyage de la mémoire de chat pour la conversation {}", conversationId);
            chatMemory.clear(conversationId);
        }
    }

    private String getConversationId(ToolContext contexte) {
        var oConversationId = contexte.getContext().get("conversationId");
        if (oConversationId instanceof String conversationId) {
            return conversationId;
        } else {
            log.warn("Aucun identifiant de conversation fourni dans le contexte de l'outil.");
            return null;
        }
    }

    @Tool(
        name = "getDossiers",
        description = "Récupère les dossiers de remboursement en fonction d'une requête en langage naturel",
        resultConverter = QueryResultToMarkdownConverter.class,
        returnDirect = true
    )
    public QueryResult getDossiersByNaturalLanguageQuery(
        @ToolParam(description = "requête en langage naturel") String naturalLanguageQuery,
        ToolContext contexte) {
        return dossiersRemboursements.findByNaturalLanguageQuery(naturalLanguageQuery, getConversationId(contexte));
    }

    private static DossierRemboursement.Id buildId(Long dossierId) {
        return DossierRemboursement.Id.of(dossierId);
    }

    public static class QueryResultToMarkdownConverter implements ToolCallResultConverter {

        @Override
        public String convert(Object result, Type returnType) {
            var queryResult = convertToQueryResult(result);

            var rows = queryResult.rows();
            if (rows.isEmpty()) {
                return "Aucun résultat trouvé.";
            }
            var columnNames = queryResult.columnNames();

            var markdownTableHeader = asMardownHeader(columnNames);
            var markdownTableSeparator = asMarkdownTableSeparator(columnNames);
            var markdownRows = rows.stream().map(row -> asMarkdownRow(row, columnNames));
            // Assemblage du tout en une seule chaîne Markdown
            return Stream.concat(Stream.of(markdownTableHeader, markdownTableSeparator), markdownRows)
                .collect(Collectors.joining("\n"));
        }

        private static String asMarkdownTableSeparator(List<String> columnNames) {
            // obtient une chaine de '-' d'une longueur égale à la longueur de la chaine header
            return columnNames.stream()
                .map(columnName -> "-".repeat(columnName.length()))
                .collect(Collectors.joining("|", "|", "|"));
        }

        private static String asMardownHeader(List<String> columnNames) {
            return columnNames.stream()
                .collect(Collectors.joining("|", "|", "|"));
        }

        private static QueryResult convertToQueryResult(Object result) {
            if (!(result instanceof QueryResult queryResult)) {
                throw new IllegalArgumentException("Expected QueryResult but got: " + result.getClass().getName());
            }
            return queryResult;
        }

        private static String asMarkdownRow(Map<String, Object> row, List<String> columnNames) {
            // Construit une ligne de résultat en utilisant les noms de colonnes
            return columnNames.stream()
                .map(columnName -> getColumnValue(row, columnName))
                .map(columnValue -> columnValue
                    .replace("|", "\\|") // Échappe les pipes pour éviter les problèmes de formatage
                    .replace("\n", "<br>") // Remplace les sauts de ligne par des balises HTML <br> pour le Markdown
                )

                .collect(Collectors.joining("|", "|", "|"));
        }

        private static String getColumnValue(Map<String, Object> row, String columnName) {
            var value = row.get(columnName);
            return value != null ? value.toString() : "null";
        }
    }

    public static class DossierRemboursementToMarkdownConverter implements ToolCallResultConverter {

        @Override
        public String convert(Object result, Type returnType) {
            var dossier = asDossierRemboursement(result);
            return """
                ## Dossier de remboursement
                - **id**: %s
                - **Date de soin**: %s
                - **Justificatifs**: %s
                - **Statut**: %s
                - **Nom du patient**: %s
                - **Numéro d'assuré**: %s
                - **Type de soin**: %s
                - **Montant réclamé**: %s €
                - **Date de création**: %s
                - **Date de décision**: %s
                - **Commentaire**: %s
                - **Decision**: %s
                """.formatted(
                dossier.id().value(),
                dossier.dateSoin(),
                dossier.justificatifs(),
                dossier.statut(),
                dossier.nom(),
                dossier.numeroAssure(),
                dossier.typeSoin(),
                dossier.montant(),
                dossier.dateDemande(),
                dossier.dateDecision(),
                dossier.commentaire(),
                dossier.decision()
            );
        }

        private static DossierRemboursement asDossierRemboursement(Object object) {
            if (!(object instanceof DossierRemboursement dossier)) {
                throw new IllegalArgumentException("Expected DossierRemboursement but got: " + object.getClass().getName());
            }
            return dossier;
        }
    }
}