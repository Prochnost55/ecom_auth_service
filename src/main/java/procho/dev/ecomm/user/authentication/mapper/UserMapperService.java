package procho.dev.ecomm.user.authentication.mapper;

import procho.dev.ecomm.user.authentication.dto.UserDto;
import procho.dev.ecomm.user.authentication.model.User;

public class UserMapperService {
    public static UserDto UserToUserDTO(User user){
        UserDto userDto= new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());

        return userDto;
    }
}
