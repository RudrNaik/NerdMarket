package onetoone.Users;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import onetoone.Laptops.Laptop;
import onetoone.Laptops.LaptopRepository;

@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    LaptopRepository laptopRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    // ==================== AUTH ENDPOINTS ==================== //

    /**
     *
     * @param user
     * @return
     *
     * Signup Page
     *
     */
    @PostMapping(path = "/signup")
    String signUp(@RequestBody User user) {
        if (user == null) {
            return failure;
        }
        // Check if email already exists
        if (userRepository.findByEmailId(user.getEmailId()) != null) {
            return "{\"message\":\"Email already in use\"}";
        }
        // Check required fields
        if (user.getName() == null || user.getEmailId() == null || user.getPassword() == null) {
            return "{\"message\":\"Name, email, and password are required\"}";
        }
        userRepository.save(user);
        return "{\"message\":\"Signup successful\", \"userId\":" + user.getId() + "}";
    }

    /// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param loginRequest
     * @return
     *
     *
     */
    @PostMapping(path = "/login")
    String login(@RequestBody User loginRequest) {
        if (loginRequest == null) {
            return failure;
        }
        // Look up user by email
        User user = userRepository.findByEmailId(loginRequest.getEmailId());
        if (user == null) {
            return "{\"message\":\"Email not found\"}";
        }
        // Check if account is active
        if (!user.getIsActive()) {
            return "{\"message\":\"Account is deactivated\"}";
        }
        // Verify password
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return "{\"message\":\"Incorrect password\"}";
        }
        return "{\"message\":\"Login successful\", \"userId\":" + user.getId()
                + ", \"name\":\"" + user.getName() + "\"}";
    }

    /// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    /// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     *
     * @param id
     * @param request
     * @return
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
        if (request.getPassword() != null) {
            user.setPassword(request.getPassword());
        }
        userRepository.save(user);
        return userRepository.findById(id);
    }

    /// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param userId
     * @param laptopId
     * @return
     */
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
     *
     * @param id
     * @return
     */
    @DeleteMapping(path = "/users/{id}")
    String deleteUser(@PathVariable int id) {
        userRepository.deleteById(id);
        return success;
    }
}