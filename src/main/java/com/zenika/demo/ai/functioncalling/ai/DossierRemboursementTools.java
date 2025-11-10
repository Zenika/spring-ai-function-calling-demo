package com.zenika.demo.ai.functioncalling.ai;

import com.zenika.demo.ai.functioncalling.remboursements.DossierRemboursement;
import com.zenika.demo.ai.functioncalling.remboursements.DossiersRemboursements;
import com.zenika.demo.ai.functioncalling.remboursements.DossiersRemboursements.QueryResult;
import com.zenika.demo.ai.functioncalling.remboursements.UseCases;
import lombok.extern.slf4j.Slf4j;
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

    public DossierRemboursementTools(DossiersRemboursements dossiersRemboursements, UseCases useCases) {
        this.dossiersRemboursements = dossiersRemboursements;
        this.useCases = useCases;
    }

    @Tool(
        name = "internalActionGetDossier",
        description = """
            uniquement pour usage interne
            """
    )
    public DossierRemboursement getDossier(
        @ToolParam(description = "id (synonymes: identifiant, numéro, n°, no) du dossier")
        Long dossierId) {
        log.debug("DossierRemboursementTools.getDossier called with dossierId: {}", dossierId);
        return dossiersRemboursements.findById(buildId(dossierId)).orElse(null);
    }

    @Tool(
        name = "internalActionMetterAJourDossier",
        description = """
            uniquement pour usage interne
            """
    )
    public DossierRemboursement mettreAJourDossier(
        @ToolParam(description = "identifiant du dossier à traiter") Long dossierId,
        @ToolParam(description = "type de traitement à appliquer (CLOTURE, REJET)") UseCases.TypeMiseAJour typeMiseAJour) {
        return useCases.traiterDossier(buildId(dossierId), typeMiseAJour);
    }

    public enum ReponseUtilisateur {
        OUI,NON,INCONNU
    }
    @Tool(
        name = "lireReponseUtilisateur",
        description = """
            Lit la réponse de l'utilisateur et retourne OUI, NON ou INCONNU.
            """
    )
    public ReponseUtilisateur lireReponseUtilisateur(
        @ToolParam(description = "texte fourni par l'utilisateur") String texteUtilisateur) {
        return switch (texteUtilisateur){
            case "oui", "yes", "y", "ok", "d'accord" -> ReponseUtilisateur.OUI;
            case "non", "no", "n", "pas d'accord" -> ReponseUtilisateur.NON;
            default -> ReponseUtilisateur.INCONNU;
        };
    }

    @Tool(
        name = "getDossiers",
        description = "Récupère les dossiers de remboursement en fonction d'une requête en langage naturel",
        resultConverter = QueryResultToMarkdownConverter.class,
        returnDirect = true
    )
    public QueryResult getDossiersByNaturalLanguageQuery(
        @ToolParam(description = "requête en langage naturel") String naturalLanguageQuery) {
        log.debug("getDossiersByNaturalLanguageQuery called with query: {}", naturalLanguageQuery);
        return dossiersRemboursements.findByNaturalLanguageQuery(naturalLanguageQuery);
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
}