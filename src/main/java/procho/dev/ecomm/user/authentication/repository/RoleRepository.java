package procho.dev.ecomm.user.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import procho.dev.ecomm.user.authentication.model.Role;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Set<Role> findAllByIdIn(List<Long> roleIds);
}