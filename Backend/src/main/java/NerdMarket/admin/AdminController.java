package NerdMarket.admin;

import NerdMarket.users.UserRepository;
import NerdMarket.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin", description = "Admin-only user management endpoints")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Get all users (admin only)")
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam Long userId) {
        try {
            List<Users> users = adminService.getAllUsers(userId);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Delete a user by ID (admin only)")
    @DeleteMapping("/users/{targetId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long targetId, @RequestParam Long userId) {
        try {
            adminService.deleteUserAccount(userId, targetId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Promote a user to admin (admin only)")
    @PutMapping("/users/{targetId}/promote")
    public ResponseEntity<?> promoteUser(@PathVariable Long targetId, @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(adminService.updateUser(userId, targetId, true, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Demote a user from admin (admin only)")
    @PutMapping("/users/{targetId}/demote")
    public ResponseEntity<?> demoteUser(@PathVariable Long targetId, @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(adminService.updateUser(userId, targetId, false, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Unlock a locked user account (admin only)")
    @PostMapping("/users/{targetId}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable Long targetId, @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(adminService.unlockUser(userId, targetId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Activate a deactivated user account (admin only)")
    @PutMapping("/users/{targetId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long targetId, @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(adminService.updateUser(userId, targetId, null, true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Deactivate a user account (admin only)")
    @PutMapping("/users/{targetId}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long targetId, @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(adminService.updateUser(userId, targetId, null, false));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Promote a user to moderator")
    @PutMapping("/admin/users/{targetId}/promote-moderator")
    public ResponseEntity<?> promoteModerator(@PathVariable Long targetId) {
        Users target = userRepository.findById(targetId).orElse(null);
        if (target == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        target.setModerator(true);
        userRepository.save(target);
        return ResponseEntity.ok(target);
    }

    @Operation(summary = "Demote a user from moderator")
    @PutMapping("/admin/users/{targetId}/demote-moderator")
    public ResponseEntity<?> demoteModerator(@PathVariable Long targetId) {
        Users target = userRepository.findById(targetId).orElse(null);
        if (target == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        target.setModerator(false);
        userRepository.save(target);
        return ResponseEntity.ok(target);
    }
}