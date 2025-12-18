package com.personalfin.server.sync.repository;

import com.personalfin.server.sync.model.SyncMetadata;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncMetadataRepository extends JpaRepository<SyncMetadata, UUID> {
    
    Optional<SyncMetadata> findByUserId(UUID userId);
    
    Optional<SyncMetadata> findByUserIdAndDeviceId(UUID userId, String deviceId);
    
    void deleteByUserId(UUID userId);
}



