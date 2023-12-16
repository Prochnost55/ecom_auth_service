package procho.dev.ecomm.user.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import procho.dev.ecomm.user.authentication.model.Session;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByTokenAndUser_Id(String token, UUID userId);
}