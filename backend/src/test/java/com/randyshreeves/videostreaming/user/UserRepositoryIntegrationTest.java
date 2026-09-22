package com.randyshreeves.videostreaming.user;

import com.randyshreeves.videostreaming.auth.dto.NewUserRegistrationRequest;
import com.randyshreeves.videostreaming.exception.UsernameAlreadyExistsException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void shouldSaveUserSuccessfully() {
        NewUserRegistrationRequest newUserRegistrationRequest = new NewUserRegistrationRequest("testuser", "hashedpassword");
        userService.registerUser(newUserRegistrationRequest);
        assertNotNull(userRepository.findByUsername("testUser"));
    }

    @Test
    void shouldFindByUsername() {
        NewUserRegistrationRequest newUserRegistrationRequest = new NewUserRegistrationRequest("testuser", "hashedpassword");
        userService.registerUser(newUserRegistrationRequest);
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void shouldNotAllowDuplicateUsername() {
        NewUserRegistrationRequest newUserRegistrationRequest = new NewUserRegistrationRequest("testuser", "hashedpassword");
        userService.registerUser(newUserRegistrationRequest);
        NewUserRegistrationRequest newUserRegistrationRequestWithDuplicateUsername = new NewUserRegistrationRequest("testuser", "hashedpassword");
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.registerUser(newUserRegistrationRequestWithDuplicateUsername));
    }

    @Test
    void shouldNotSaveUserWithoutUsername() {
        User user = new User(null, "hashedpassword", Role.ROLE_USER);
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(user));
    }

    @Test
    void shouldNotSaveUserWithoutPassword() {
        User user = new User("testUser", null, Role.ROLE_USER);
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(user));
    }

}
