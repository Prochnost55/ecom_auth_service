package procho.dev.ecomm.user.authentication.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import procho.dev.ecomm.user.authentication.dto.SetUserRolesRequestDto;
import procho.dev.ecomm.user.authentication.dto.UserDto;
import procho.dev.ecomm.user.authentication.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserDetails(@PathVariable("id") String userId) {
        UserDto userDto = userService.getUserDetails(UUID.fromString(userId));

        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<UserDto> setUserRoles(@PathVariable("id") String userId, @RequestBody SetUserRolesRequestDto request) {

        UserDto userDto = userService.setUserRoles(UUID.fromString(userId), request.getRoleIds());

        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }


}