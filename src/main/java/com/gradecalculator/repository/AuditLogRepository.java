package com.gradecalculator.repository;

import com.gradecalculator.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityNameAndEntityId(String entityName, String entityId);

    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:fromTime IS NULL OR a.timestamp >= :fromTime)
          AND (:toTime   IS NULL OR a.timestamp <= :toTime)
          AND (:action     IS NULL OR UPPER(a.action)     = UPPER(:action))
          AND (:entityName IS NULL OR UPPER(a.entityName) = UPPER(:entityName))
          AND (:username   IS NULL OR LOWER(a.username)   = LOWER(:username))
        ORDER BY a.timestamp DESC
        """)
    Stream<AuditLog> streamFiltered(
            LocalDateTime fromTime,
            LocalDateTime toTime,
            String action,
            String entityName,
            String username,
            Pageable pageable);
}
