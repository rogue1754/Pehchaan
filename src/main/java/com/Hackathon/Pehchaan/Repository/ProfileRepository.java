
package com.Hackathon.Pehchaan.Repository;

import com.Hackathon.Pehchaan.Entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository
        extends JpaRepository<Profile, Long> {

    boolean existsByAadhaarHash(String aadhaarHash);

    boolean existsByPanNumber(String panNumber);
}
