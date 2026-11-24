package com.careerassistant.user;

import com.careerassistant.common.ApiException;
import com.careerassistant.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserAccountRepository users;
    private final UserProfileRepository profiles;
    private final PasswordEncoder passwordEncoder;
    public UserController(UserAccountRepository users, UserProfileRepository profiles, PasswordEncoder passwordEncoder) {
        this.users = users; this.profiles = profiles; this.passwordEncoder = passwordEncoder;
    }
    @GetMapping("/profile")
    public ProfileResponse getProfile() {
        UserAccount user = CurrentUser.require();
        return map(user, profileFor(user));
    }
    @PutMapping("/profile")
    public ProfileResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserAccount user = CurrentUser.require();
        if (!user.getEmail().equalsIgnoreCase(request.email()) && users.existsByEmail(request.email().toLowerCase())) {
            throw ApiException.badRequest("That email address is already in use.");
        }
        user.setName(request.name().trim()); user.setEmail(request.email().trim()); users.save(user);
        UserProfile profile = profileFor(user);
        profile.setPhone(blankToNull(request.phone())); profile.setLocation(blankToNull(request.location()));
        profile.setEducation(blankToNull(request.education())); profile.setTargetRole(blankToNull(request.targetRole()));
        profile.setCareerPreferences(blankToNull(request.careerPreferences())); profiles.save(profile);
        return map(user, profile);
    }
    @PutMapping("/password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        UserAccount user = CurrentUser.require();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("The current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword())); users.save(user);
    }
    private UserProfile profileFor(UserAccount user) {
        return profiles.findByUserId(user.getId()).orElseThrow(() -> ApiException.notFound("Profile not found."));
    }
    private ProfileResponse map(UserAccount user, UserProfile profile) {
        return new ProfileResponse(user.getId(), user.getName(), user.getEmail(), profile.getPhone(), profile.getLocation(),
                profile.getEducation(), profile.getTargetRole(), profile.getCareerPreferences());
    }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    public record UpdateProfileRequest(@Size(min = 1, max = 120) String name, @Email String email, @Size(max = 40) String phone,
                                       @Size(max = 160) String location, @Size(max = 500) String education,
                                       @Size(max = 160) String targetRole, @Size(max = 5000) String careerPreferences) {}
    public record ChangePasswordRequest(@Size(min = 8, max = 72) String currentPassword, @Size(min = 8, max = 72) String newPassword) {}
    public record ProfileResponse(Long id, String name, String email, String phone, String location, String education,
                                  String targetRole, String careerPreferences) {}
}
