
package com.Hackathon.Pehchaan.Repository;

import com.Hackathon.Pehchaan.Entity.ImportFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportFileRepository
        extends JpaRepository<ImportFile, Long> {

    List<ImportFile> findByImportSessionId(Long importSessionId);
}
