package NerdMarket.admin;

import NerdMarket.users.UserRepository;
import NerdMarket.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    private Users requireAdmin(Long userId) {
        Users user = userRepository.findUsersById(userId);
        if (user == null || !user.isAdmin()) {
            throw new RuntimeException("Unauthorized - admin access required");
        }
        return user;
    }

    private Users requireUser(Long targetId) {
        Users user = userRepository.findUsersById(targetId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    public List<Users> getAllUsers(Long userId) {
        requireAdmin(userId);
        return userRepository.findAll();
    }

    public void deleteUserAccount(Long userId, Long targetId) {
        requireAdmin(userId);
        if (userId.equals(targetId)) {
            throw new RuntimeException("Can't delete your own account");
        }
        requireUser(targetId);
        userRepository.deleteById(targetId);
    }

    public Users unlockUser(Long userId, Long targetId) {
        requireAdmin(userId);
        Users target = requireUser(targetId);
        target.setLocked(false);
        target.setLoginAttempts(0);
        return userRepository.save(target);
    }

    public Users updateUser(Long userId, Long targetId, Boolean admin, Boolean active) {
        requireAdmin(userId);
        Users target = requireUser(targetId);

        if (admin != null) {
            if (!admin && userId.equals(targetId)) {
                throw new RuntimeException("Can't remove your own admin status");
            }
            target.setAdmin(admin);
        }

        if (active != null) {
            target.setActive(active);
        }

        return userRepository.save(target);
    }
}