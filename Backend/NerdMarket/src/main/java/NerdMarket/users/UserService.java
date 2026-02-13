package NerdMarket.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final int MAX_ATTEMPTS = 3;

    public Users signup(Users user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        user.setPassword(encoder.encode(user.getPassword()));
        user.setActive(true);
        user.setAdmin(false);
        user.setLoginAttempts(0);
        user.setLocked(false);

        return userRepository.save(user);
    }

    public Users login(String usernameOrEmail, String password) {
        Users user = userRepository.findByUsername(usernameOrEmail);
        if (user == null) {
            user = userRepository.findByEmail(usernameOrEmail);
        }

        if (user == null) {
            throw new RuntimeException("Invalid username or email");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Account Deactivated");
        }

        if (user.isLocked()) {
            throw new RuntimeException("Account is locked due to too many failed attempts. Reset your password to unlock.");
        }

        if (!encoder.matches(password, user.getPassword())) {
            user.setLoginAttempts(user.getLoginAttempts() + 1);

            if (user.getLoginAttempts() >= MAX_ATTEMPTS) {
                user.setLocked(true);
            }

            userRepository.save(user);
            throw new RuntimeException("Wrong password. Attempt " + user.getLoginAttempts() + " of " + MAX_ATTEMPTS);
        }

        user.setLoginAttempts(0);
        userRepository.save(user);

        return user;
    }

    public Users changePassword(Long id, String oldPassword, String newPassword) {
        Users user = userRepository.findUsersById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(encoder.encode(newPassword));
        return userRepository.save(user);
    }

    public Users resetPassword(String email, String oldPassword, String newPassword) {
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("No account with that email");
        }

        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(encoder.encode(newPassword));
        user.setLocked(false);
        user.setLoginAttempts(0);
        return userRepository.save(user);
    }

    public Users deactivateAccount(Long id) {
        Users user = userRepository.findUsersById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setActive(false);
        return userRepository.save(user);
    }

    public Users activateAccount(Long id) {
        Users user = userRepository.findUsersById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setActive(true);
        return userRepository.save(user);
    }

    public Users setAdmin(Long id, boolean isAdmin) {
        Users user = userRepository.findUsersById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setAdmin(isAdmin);
        return userRepository.save(user);
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Users getUserById(Long id) {
        Users user = userRepository.findUsersById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
