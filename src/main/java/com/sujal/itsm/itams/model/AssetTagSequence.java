package com.sujal.itsm.itams.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Single-row global counter that drives AST-XXXXXX generation.
 * Pessimistic locking guarantees uniqueness under concurrency.
 */
@Entity
@Table(name = "asset_tag_sequence")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AssetTagSequence {

    @Id
    @Column(name = "seq_name", length = 32)
    private String seqName;

    @Column(name = "next_val", nullable = false)
    private Long nextVal;
}