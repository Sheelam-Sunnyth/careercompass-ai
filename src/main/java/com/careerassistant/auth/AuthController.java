package com.careerassistant.auth;

import com.careerassistant.common.ApiException;
import com.careerassistant.security.JwtService;
import com.careerassistant.user.UserAccount;
import com.careerassistant.user.UserAccountRepository;
import com.careerassistant.user.UserProfile;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserAccountRepository users;
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    public AuthController(UserAccountRepository users, EntityManager entityManager, PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users; this.entityManager = entityManager; this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager; this.jwtService = jwtService;
    }
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (users.existsByEmail(request.email().toLowerCase())) throw ApiException.badRequest("An account with this email already exists.");
        UserAccount user = new UserAccount();
        user.setName(request.name().trim()); user.setEmail(request.email().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        users.save(user);
        UserProfile profile = new UserProfile(); profile.setUser(user); entityManager.persist(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response(user));
    }
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        } catch (RuntimeException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
        UserAccount user = users.findByEmail(request.email().toLowerCase()).orElseThrow();
        return response(user);
    }
    private AuthResponse response(UserAccount user) {
        return new AuthResponse(jwtService.issue(user), new UserSummary(user.getId(), user.getName(), user.getEmail()));
    }
    public record RegisterRequest(@NotBlank @Size(max = 120) String name, @NotBlank @Email String email,
                                  @NotBlank @Size(min = 8, max = 72) String password) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record AuthResponse(String token, UserSummary user) {}
    public record UserSummary(Long id, String name, String email) {}
}
