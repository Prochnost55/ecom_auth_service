package procho.dev.ecomm.user.authentication.service;

import org.springframework.stereotype.Service;
import procho.dev.ecomm.user.authentication.model.Role;
import procho.dev.ecomm.user.authentication.repository.RoleRepository;


@Service
public class RoleService {
    private RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role createRole(String name) {
        Role role = new Role();
        role.setRole(name);

        return roleRepository.save(role);
    }
}
