package procho.dev.ecomm.user.authentication.service;

import org.springframework.stereotype.Service;
import procho.dev.ecomm.user.authentication.dto.UserDto;
import procho.dev.ecomm.user.authentication.model.Role;
import procho.dev.ecomm.user.authentication.model.User;
import procho.dev.ecomm.user.authentication.repository.RoleRepository;
import procho.dev.ecomm.user.authentication.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public UserDto getUserDetails(UUID userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return null;
        }

        return UserDto.from(userOptional.get());
    }

    public UserDto setUserRoles(UUID userId, List<Long> roleIds) {
        Optional<User> userOptional = userRepository.findById(userId);
        Set<Role> roles = roleRepository.findAllByIdIn(roleIds);

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        return UserDto.from(savedUser);
    }
}
