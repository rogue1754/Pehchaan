
package com.Hackathon.Pehchaan.Repository;

import com.Hackathon.Pehchaan.Entity.GuidanceProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuidanceProgressRepository
        extends JpaRepository<GuidanceProgress, Long> {

    List<GuidanceProgress> findByGuidanceType(String guidanceType);

    Optional<GuidanceProgress> findByGuidanceTypeAndStepKey(
            String guidanceType, String stepKey);
}
