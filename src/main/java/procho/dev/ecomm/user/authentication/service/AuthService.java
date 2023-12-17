package procho.dev.ecomm.user.authentication.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.MacAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.bind.annotation.PathVariable;
import procho.dev.ecomm.user.authentication.dto.UserDto;
import procho.dev.ecomm.user.authentication.exceptions.InvalidCredentialException;
import procho.dev.ecomm.user.authentication.exceptions.InvalidSessionException;
import procho.dev.ecomm.user.authentication.exceptions.InvalidTokenException;
import procho.dev.ecomm.user.authentication.exceptions.UserNotFoundException;
import procho.dev.ecomm.user.authentication.mapper.UserMapperService;
import procho.dev.ecomm.user.authentication.model.Session;
import procho.dev.ecomm.user.authentication.model.SessionStatus;
import procho.dev.ecomm.user.authentication.model.User;
import procho.dev.ecomm.user.authentication.repository.SessionRepository;
import procho.dev.ecomm.user.authentication.repository.UserRepository;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.*;

@Service
public class AuthService {
    private static MacAlgorithm alg = Jwts.SIG.HS256; // define the algo to use
    private static SecretKey secretKey = alg.key().build(); // generate secret key
    private static int THREE_DAYS = 3 * 24 * 60 * 60 * 1000;
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

        // close the previous active sessions of the user
        closePreviousSessions(user.getId());

        // generate JWT token
        // A JWT token contains header, claims(body) and token. The header contains the encryption algo used. Header and Body are parse able
        Map<String, Object> jsonForJWT = new HashMap<>();
        jsonForJWT.put("email", user.getEmail());
        jsonForJWT.put("roles", user.getRoles());
        jsonForJWT.put("createdAt", new Date());
        jsonForJWT.put("expiresAt", THREE_DAYS);
        String token = Jwts.builder()
                .claims(jsonForJWT) // add claims ie body
                .signWith(secretKey, alg) // add algo and key
                .compact(); // build token

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
            throw new InvalidSessionException();
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
        // check if session is present and is not in ended state
        if (sessionOptional.isEmpty() || sessionOptional.get().getSessionStatus().equals(SessionStatus.ENDED)) {
            throw new InvalidTokenException("Token is expired");
        }

        // check if token is expired
        JwtParser jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .build();
        try {
            Jws<Claims> claims = jwtParser.parseSignedClaims(token);
            Map<String, Object> payload = claims.getPayload();
            Date currTime = new Date();
            long createdAt = (long)payload.get("createdAt");
            long expiresAt = createdAt + (int)payload.get("expiresAt");

            if(currTime.after(new Date(expiresAt))){
                throw new InvalidTokenException("Token is expired");
            };
        } catch(Exception e){
            throw new InvalidTokenException("Token is not valid");
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
