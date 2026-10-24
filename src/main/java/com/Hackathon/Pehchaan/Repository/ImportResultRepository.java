
package com.Hackathon.Pehchaan.Repository;

import com.Hackathon.Pehchaan.Entity.ImportResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportResultRepository
        extends JpaRepository<ImportResult, Long> {

    List<ImportResult> findByImportSessionId(Long importSessionId);
}
