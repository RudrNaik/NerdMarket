package NerdMarket.admin;

import NerdMarket.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam Long userId) {
        try {
            List<Users> users = adminService.getAllUsers(userId);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/users/{targetId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long targetId, @RequestParam Long userId) {
        try {
            adminService.deleteUserAccount(userId, targetId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/{targetId}/promote")
    public ResponseEntity<?> promoteUser(@PathVariable Long targetId, @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(adminService.updateUser(userId, targetId, true, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/{targetId}/demote")
    public ResponseEntity<?> demoteUser(@PathVariable Long targetId, @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(adminService.updateUser(userId, targetId, false, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/{targetId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long targetId, @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(adminService.updateUser(userId, targetId, null, true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/{targetId}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long targetId, @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(adminService.updateUser(userId, targetId, null, false));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}