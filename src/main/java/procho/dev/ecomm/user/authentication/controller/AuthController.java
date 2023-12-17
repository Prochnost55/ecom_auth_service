package procho.dev.ecomm.user.authentication.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import procho.dev.ecomm.user.authentication.dto.*;
import procho.dev.ecomm.user.authentication.exceptions.UserNotFoundException;
import procho.dev.ecomm.user.authentication.model.Session;
import procho.dev.ecomm.user.authentication.model.SessionStatus;
import procho.dev.ecomm.user.authentication.model.User;
import procho.dev.ecomm.user.authentication.service.AuthService;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto request) {
        return authService.login(request.getEmail(), request.getPassword());
//        return null;
    }

    @PostMapping("/logout/{userId}")
    public ResponseEntity<Void> logout(@PathVariable("userId") String userId, @RequestHeader("token") String token) {
        return authService.logout(token, userId);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@RequestBody SignUpRequestDto request) {
        UserDto userDto = authService.signUp(request.getEmail(), request.getPassword());
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PostMapping("/validate/{userId}")
    public ResponseEntity<SessionStatus> validateToken(@PathVariable("userId") String userId, @RequestHeader("token") String token) {
        SessionStatus sessionStatus = authService.validate(token, userId);

        return new ResponseEntity<>(sessionStatus, HttpStatus.OK);
    }


    //below APIs are only for learning purposes, should not be present in actual systems
    @GetMapping("/session")
    public ResponseEntity<List<Session>> getAllSession(){
        return authService.getAllSession();
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(){
        return authService.getAllUsers();
    }

}
