
package com.Hackathon.Pehchaan.Repository;

import com.Hackathon.Pehchaan.Entity.IdentityRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentityRecordRepository
        extends JpaRepository<IdentityRecord, Long> {
}
