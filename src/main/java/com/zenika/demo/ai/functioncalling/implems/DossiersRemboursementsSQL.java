package com.zenika.demo.ai.functioncalling.implems;

import com.zenika.demo.ai.functioncalling.ai.DossierRemboursementSQLPrompter;
import com.zenika.demo.ai.functioncalling.implems.DossierRemboursementSQLSpring.DossierRemboursementSQL;
import com.zenika.demo.ai.functioncalling.remboursements.DossierRemboursement;
import com.zenika.demo.ai.functioncalling.remboursements.DossiersRemboursements;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Optional;

@Repository
@Primary
@Slf4j
public class DossiersRemboursementsSQL implements DossiersRemboursements {

    private final DossierRemboursementSQLPrompter queryPrompter;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final DossierRemboursementSQLSpring repository;

    private static final DossierRemboursementSQLMapper MAPPER = DossierRemboursementSQLMapper.INSTANCE;

    public DossiersRemboursementsSQL(DossierRemboursementSQLPrompter queryPrompter, NamedParameterJdbcTemplate jdbcTemplate, DossierRemboursementSQLSpring repository) {
        this.queryPrompter = queryPrompter;
        this.jdbcTemplate = jdbcTemplate;
        this.repository = repository;
    }

    @Override
    public void save(DossierRemboursement dossier) {
        repository.save(MAPPER.toSQL(dossier));
    }

    @Override
    public Optional<DossierRemboursement> findById(DossierRemboursement.Id id) {
        return repository.findById(id.value()).map(MAPPER::fromSQL);
    }

    @SneakyThrows
    @Override
    public QueryResult findByNaturalLanguageQuery(String query) {
        var sqlQuery = queryPrompter.getSQL(query);
        log.debug("SQL query: {}", sqlQuery);
        var rows = jdbcTemplate.query(
            sqlQuery.sql(),
            sqlQuery.parameters(),
            (rs, rowNum) -> buildRow(rs));
        if (rows.isEmpty()) {
            log.info("No results found for query: {}", sqlQuery.sql());
            return new QueryResult(Collections.emptyList(), Collections.emptyList());
        }
        // On suppose que toutes les lignes ont les mêmes colonnes,
        // on peut donc utiliser la première ligne pour obtenir les noms de colonnes.
        var first = rows.getFirst();
        var columnNames = first.sequencedKeySet().stream().toList();
        return new QueryResult(columnNames, rows);
    }

    private static LinkedHashMap<String, Object> buildRow(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        LinkedHashMap<String,Object> row = LinkedHashMap.newLinkedHashMap(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            Object value = rs.getObject(i);
            row.put(columnName, value);
        }
        return row;
    }

    @Mapper
    interface DossierRemboursementSQLMapper {
        DossierRemboursementSQLMapper INSTANCE = Mappers.getMapper(DossierRemboursementSQLMapper.class);

        @Mapping(source = "id.value", target = "id")
        DossierRemboursementSQL toSQL(DossierRemboursement dossierRemboursement);

        @Mapping(source = "id", target = "id.value")
        DossierRemboursement fromSQL(DossierRemboursementSQL dossierRemboursementSQL);
    }
}
