package com.sujal.itsm.itams.service;

import com.sujal.itsm.itams.model.AssetTagSequence;
import com.sujal.itsm.itams.repository.AssetTagSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetTagService {

    public static final String GLOBAL_SEQ = "GLOBAL";

    private final AssetTagSequenceRepository sequenceRepository;

    /**
     * Generates the next global, category-agnostic asset identifier: AST-000125.
     * MANDATORY propagation = runs inside the caller's transaction, so a failed
     * asset save rolls the counter back with it (no wasted numbers on errors).
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public String nextAssetTag() {
        AssetTagSequence seq = sequenceRepository.lockByName(GLOBAL_SEQ)
                .orElseGet(() -> sequenceRepository.saveAndFlush(new AssetTagSequence(GLOBAL_SEQ, 1L)));

        long value = seq.getNextVal();
        seq.setNextVal(value + 1);
        return String.format("AST-%06d", value);
    }
}