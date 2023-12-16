package procho.dev.ecomm.user.authentication.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;
import procho.dev.ecomm.user.authentication.dto.UserDto;
import procho.dev.ecomm.user.authentication.exceptions.InvalidCredentialException;
import procho.dev.ecomm.user.authentication.exceptions.UserNotFoundException;
import procho.dev.ecomm.user.authentication.mapper.UserMapperService;
import procho.dev.ecomm.user.authentication.model.Session;
import procho.dev.ecomm.user.authentication.model.SessionStatus;
import procho.dev.ecomm.user.authentication.model.User;
import procho.dev.ecomm.user.authentication.repository.SessionRepository;
import procho.dev.ecomm.user.authentication.repository.UserRepository;

import java.util.*;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    // BCryptPasswordEncoder is used to convert password into encrypted string which can not be decrypted back.
    // Also, each time you encode a string, the encoded string will be different.
    // bCrypt algo is responsible for comparing the encoded and raw string.
    // This class comes with Spring security library
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // Note: To inject BCryptPasswordEncoder in your service bean you will have to create spring security config file.
    // See SpringSecurity.java

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public ResponseEntity<UserDto> login(String email, String password) {
        // Fetch user from DB
        Optional<User> userOptional = userRepository.findByEmail(email);

        // If user is not present then return UserNotFoundException
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("No user found with email " + email);
        }

        User user = userOptional.get();

        // compare password and if not matching then throw InvalidCredentialException
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialException("Invalid credentials." + email + " " + password);
        }

        closePreviousSessions(user.getId());
        // generate token
        String token = RandomStringUtils.randomAlphanumeric(30);

        // create a session
        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setToken(token);
        session.setUser(user);
        session.setLoginAt(new Date());
        sessionRepository.save(session);

        // generate the response
        UserDto userDto = UserMapperService.UserToUserDTO(user);

        // Map<String, String> headers = new HashMap<>();
        // headers.put(HttpHeaders.SET_COOKIE, token);

        // set up the headers
        // MultiValueMapAdapter is a hashmap in which the key can contain multiple values
        // MultiValueMapAdapter is available with spring
        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, token);
        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, headers, HttpStatus.OK);
        return response;
    }

    public ResponseEntity<Void> logout(String token, String userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, UUID.fromString(userId));

        if (sessionOptional.isEmpty()) {
            return null;
        }

        Session session = sessionOptional.get();

        session.setSessionStatus(SessionStatus.ENDED);

        sessionRepository.save(session);

        return ResponseEntity.ok().build();
    }

    public UserDto signUp(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        User savedUser = userRepository.save(user);
        return UserDto.from(savedUser);
    }

    public SessionStatus validate(String token, String userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, UUID.fromString(userId));

        if (sessionOptional.isEmpty()) {
            return null;
        }

        return SessionStatus.ACTIVE;
    }

    public void closePreviousSessions(UUID userId){
        List<Session> activeSessions = sessionRepository.findSessionsByUserIdAndSessionStatus(userId, SessionStatus.ACTIVE);
        for(Session eachSession: activeSessions){
            logout(eachSession.getToken(), userId.toString());
        }
    }

    //---------------------------- to be removed -----------------------
    public ResponseEntity<List<Session>> getAllSession(){
        List<Session> sessions = sessionRepository.findAll();
        return ResponseEntity.ok(sessions);
    }

    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userRepository.findAll());
    }
    // ----------------------------------------------------------------
}
