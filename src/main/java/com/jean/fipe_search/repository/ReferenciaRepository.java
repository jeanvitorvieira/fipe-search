package com.jean.fipe_search.repository;

import com.jean.fipe_search.dto.ConsultaFipeDTO;
import com.jean.fipe_search.entity.ReferenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReferenciaRepository extends JpaRepository<ReferenciaEntity, Long> {

    @Query("""
            SELECT new com.jean.fipe_search.dto.ConsultaFipeDTO(
                ma.nome, mo.nome, r.anoModelo, r.preco, r.mesReferencia)
            FROM ReferenciaEntity r
                JOIN r.modelo mo
                JOIN mo.marca ma
            WHERE r.modelo.id = :modeloId AND r.anoModelo = :anoModelo
            ORDER BY r.mesReferencia DESC
            """)
    List<ConsultaFipeDTO> findReferencias(
            @Param("modeloId") Long modeloId,
            @Param("anoModelo") Integer anoModelo);
}