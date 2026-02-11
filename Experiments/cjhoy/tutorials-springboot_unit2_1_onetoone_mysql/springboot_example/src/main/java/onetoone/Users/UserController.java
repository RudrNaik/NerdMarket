package onetoone.Users;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import onetoone.Laptops.Laptop;
import onetoone.Laptops.LaptopRepository;

/**
 * UserController with full auth features:
 *
 * AUTH:
 *   POST /signup              - Register (BCrypt hashed password)
 *   POST /login               - Login with lockout protection
 *
 * PASSWORD:
 *   PUT  /users/{id}/password - Change password (requires old password)
 *
 * ACCOUNT MANAGEMENT:
 *   PUT  /users/{id}/deactivate  - Soft delete (deactivate account)
 *   PUT  /users/{id}/reactivate  - Reactivate account
 *   PUT  /users/{id}/unlock      - Admin: manually unlock a locked account
 *   DELETE /users/{id}           - Hard delete (permanent)
 *
 * CRUD:
 *   GET    /users             - List all users
 *   GET    /users/{id}        - Get user by ID
 *   POST   /users             - Create user (basic, no hashing)
 *   PUT    /users/{id}        - Update user profile
 *   PUT    /users/{userId}/laptops/{laptopId} - Assign laptop
 */
@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    LaptopRepository laptopRepository;

    // BCrypt encoder for password hashing
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    // ==================== AUTH ENDPOINTS ==================== //

    /**
     * SIGNUP - Register with hashed password
     *
     * POST /signup
     * {
     *   "name": "John",
     *   "emailId": "john@mail.com",
     *   "password": "mypass123"
     * }
     *
     * The password is hashed with BCrypt before saving.
     * Even if someone reads the database, they can't see the real password.
     */
    @PostMapping(path = "/signup")
    String signUp(@RequestBody User user) {
        if (user == null) {
            return failure;
        }
        // Check required fields
        if (user.getName() == null || user.getEmailId() == null || user.getPassword() == null) {
            return "{\"message\":\"Name, email, and password are required\"}";
        }
        // Check if email already exists
        if (userRepository.findByEmailId(user.getEmailId()) != null) {
            return "{\"message\":\"Email already in use\"}";
        }

        // Hash the password before saving
        user.setPassword(encoder.encode(user.getPassword()));

        userRepository.save(user);
        return "{\"message\":\"Signup successful\", \"userId\":" + user.getId() + "}";
    }

    /**
     * LOGIN - Authenticate with lockout protection
     *
     * POST /login
     * {
     *   "emailId": "john@mail.com",
     *   "password": "mypass123"
     * }
     *
     * Security features:
     *  1. Checks if account exists
     *  2. Checks if account is active (not deactivated)
     *  3. Checks if account is locked (too many failed attempts)
     *  4. Verifies password with BCrypt
     *  5. Tracks failed attempts → auto-locks after 5 fails for 15 min
     *  6. Records successful login (count + timestamp)
     */
    @PostMapping(path = "/login")
    String login(@RequestBody User loginRequest) {
        if (loginRequest == null) {
            return failure;
        }

        // 1. Look up user by email
        User user = userRepository.findByEmailId(loginRequest.getEmailId());
        if (user == null) {
            return "{\"message\":\"Email not found\"}";
        }

        // 2. Check if account is active
        if (!user.getIsActive()) {
            return "{\"message\":\"Account is deactivated. Contact admin.\"}";
        }

        // 3. Check if account is locked
        if (user.isLockedOut()) {
            return "{\"message\":\"Account is locked. Try again after "
                    + user.getLockoutUntil() + "\"}";
        }

        // 4. Verify password using BCrypt
        if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // Record the failed attempt (may trigger lockout)
            user.recordFailedAttempt();
            userRepository.save(user);

            int remaining = 5 - user.getFailedAttempts();
            if (remaining > 0) {
                return "{\"message\":\"Incorrect password. " + remaining + " attempts remaining.\"}";
            } else {
                return "{\"message\":\"Account locked due to too many failed attempts. Try again in 15 minutes.\"}";
            }
        }

        // 5. Success! Record the login
        user.recordSuccessfulLogin();
        userRepository.save(user);

        return "{\"message\":\"Login successful\""
                + ", \"userId\":" + user.getId()
                + ", \"name\":\"" + user.getName() + "\""
                + ", \"loginCount\":" + user.getLoginCount()
                + "}";
    }

    // ==================== PASSWORD MANAGEMENT ==================== //

    /**
     * CHANGE PASSWORD - Requires old password for verification
     *
     * PUT /users/{id}/password
     * {
     *   "emailId": "john@mail.com",     <-- used to pass old password
     *   "password": "newPassword123"     <-- the new password
     *   "name": "oldPassword123"         <-- hack: using name field for old password
     * }
     *
     * NOTE: In a real app you'd create a DTO (Data Transfer Object) class
     * with oldPassword/newPassword fields instead of reusing User fields.
     * This is a shortcut to avoid creating extra classes for the tutorial.
     */
    @PutMapping("/users/{id}/password")
    String changePassword(@PathVariable int id, @RequestBody User request) {
        User user = userRepository.findById(id);
        if (user == null) {
            return "{\"message\":\"User not found\"}";
        }

        // Verify old password (passed in the "name" field as a workaround)
        String oldPassword = request.getName();
        if (oldPassword == null || !encoder.matches(oldPassword, user.getPassword())) {
            return "{\"message\":\"Old password is incorrect\"}";
        }

        // Set new hashed password
        user.setPassword(encoder.encode(request.getPassword()));
        userRepository.save(user);
        return "{\"message\":\"Password changed successfully\"}";
    }

    // ==================== ACCOUNT MANAGEMENT ==================== //

    /**
     * DEACTIVATE - Soft delete (account still exists but can't login)
     * PUT /users/{id}/deactivate
     */
    @PutMapping("/users/{id}/deactivate")
    String deactivateUser(@PathVariable int id) {
        User user = userRepository.findById(id);
        if (user == null) {
            return "{\"message\":\"User not found\"}";
        }
        user.setIfActive(false);
        userRepository.save(user);
        return "{\"message\":\"Account deactivated\"}";
    }

    /**
     * REACTIVATE - Re-enable a deactivated account
     * PUT /users/{id}/reactivate
     */
    @PutMapping("/users/{id}/reactivate")
    String reactivateUser(@PathVariable int id) {
        User user = userRepository.findById(id);
        if (user == null) {
            return "{\"message\":\"User not found\"}";
        }
        user.setIfActive(true);
        user.setFailedAttempts(0);   // Also reset failed attempts
        user.setLockoutUntil(null);  // And clear any lockout
        userRepository.save(user);
        return "{\"message\":\"Account reactivated\"}";
    }

    /**
     * UNLOCK - Admin manually unlocks a locked account
     * PUT /users/{id}/unlock
     */
    @PutMapping("/users/{id}/unlock")
    String unlockUser(@PathVariable int id) {
        User user = userRepository.findById(id);
        if (user == null) {
            return "{\"message\":\"User not found\"}";
        }
        user.setFailedAttempts(0);
        user.setLockoutUntil(null);
        userRepository.save(user);
        return "{\"message\":\"Account unlocked\"}";
    }

    // ==================== CRUD ENDPOINTS ==================== //

    @GetMapping(path = "/users")
    List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping(path = "/users/{id}")
    User getUserById(@PathVariable int id) {
        return userRepository.findById(id);
    }

    @PostMapping(path = "/users")
    String createUser(@RequestBody User user) {
        if (user == null)
            return failure;
        userRepository.save(user);
        return success;
    }

    /**
     * UPDATE PROFILE - Change name or email (not password, use /password endpoint)
     */
    @PutMapping("/users/{id}")
    User updateUser(@PathVariable int id, @RequestBody User request) {
        User user = userRepository.findById(id);
        if (user == null)
            return null;
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmailId() != null) {
            user.setEmailId(request.getEmailId());
        }
        userRepository.save(user);
        return userRepository.findById(id);
    }

    @PutMapping("/users/{userId}/laptops/{laptopId}")
    String assignLaptopToUser(@PathVariable int userId, @PathVariable int laptopId) {
        User user = userRepository.findById(userId);
        Laptop laptop = laptopRepository.findById(laptopId);
        if (user == null || laptop == null)
            return failure;
        laptop.setUser(user);
        user.setLaptop(laptop);
        userRepository.save(user);
        return success;
    }

    /**
     * HARD DELETE - Permanently remove from database
     * For soft delete, use /users/{id}/deactivate instead
     */
    @DeleteMapping(path = "/users/{id}")
    String deleteUser(@PathVariable int id) {
        userRepository.deleteById(id);
        return success;
    }
}