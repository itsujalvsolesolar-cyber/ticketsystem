package com.sujal.itsm.itams.service;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.enums.AssetHistoryAction;
import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.model.AssetHistory;
import com.sujal.itsm.itams.repository.AssetHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetHistoryService {

    private final AssetHistoryRepository historyRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public AssetHistory record(Asset asset, AssetHistoryAction action,
                               String oldValue, String newValue, String remarks) {
        AppUser user = null;
        String name = "SYSTEM";
        try {
            user = currentUserService.getCurrentUser();
            name = user.getUsername();
        } catch (Exception ignored) { }

        AssetHistory history = AssetHistory.builder()
                .asset(asset)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .remarks(remarks)
                .performedBy(user)
                .performedByName(name)
                .build();
        return historyRepository.save(history);
    }

    public List<AssetHistory> getHistoryForAsset(Long assetId) {
        return historyRepository.findByAssetIdOrderByCreatedAtDesc(assetId);
    }
}