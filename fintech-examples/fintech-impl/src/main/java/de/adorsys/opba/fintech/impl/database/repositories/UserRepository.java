package de.adorsys.opba.fintech.impl.database.repositories;

import de.adorsys.opba.fintech.impl.database.entities.SessionEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<SessionEntity, String> {
    Optional<SessionEntity> findByXsrfToken(String xsrfToken);
    void deleteByXsrfToken(String xsrfToken);
}
