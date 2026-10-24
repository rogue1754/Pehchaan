
package com.Hackathon.Pehchaan.Repository;

import com.Hackathon.Pehchaan.Entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository
        extends JpaRepository<Report, Long> {
}
