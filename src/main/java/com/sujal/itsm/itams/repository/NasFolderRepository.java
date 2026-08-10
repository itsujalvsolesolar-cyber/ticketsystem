package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.NasFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NasFolderRepository extends JpaRepository<NasFolder, Long> {
    Optional<NasFolder> findByFolderName(String folderName);
}