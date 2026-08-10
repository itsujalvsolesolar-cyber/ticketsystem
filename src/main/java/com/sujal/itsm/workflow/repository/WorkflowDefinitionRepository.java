package com.sujal.itsm.workflow.repository;

import com.sujal.itsm.workflow.enums.WorkflowModuleType;
import com.sujal.itsm.workflow.model.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {

    @Query("SELECT w FROM WorkflowDefinition w LEFT JOIN FETCH w.steps WHERE w.moduleType = :moduleType AND w.isActive = true")
    Optional<WorkflowDefinition> findActiveByModuleType(WorkflowModuleType moduleType);
}