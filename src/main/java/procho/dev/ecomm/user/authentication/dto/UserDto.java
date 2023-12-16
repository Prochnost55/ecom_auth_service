package procho.dev.ecomm.user.authentication.dto;

import lombok.Getter;
import lombok.Setter;
import procho.dev.ecomm.user.authentication.model.Role;
import procho.dev.ecomm.user.authentication.model.User;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserDto {
    private String email;
    private Set<Role> roles = new HashSet<>();

    public static UserDto from(User user) {
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());

        return userDto;
    }

}