package com.sujal.itsm.workflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_steps")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkflowStep {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Min(1)
    @Column(nullable = false)
    private Integer stepOrder; // 1, 2, 3...

    @NotBlank
    @Column(nullable = false, length = 50)
    private String approverRole; // e.g., "IT MANAGER", "DEPARTMENT HEAD"

    @Builder.Default
    @Column(nullable = false)
    private Boolean isRequired = true;

    @CreationTimestamp @Column(updatable = false)
    private LocalDateTime createdAt;
}