package onetoone.Users;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

import onetoone.Laptops.Laptop;

/**
 * User entity with:
 *  - BCrypt password hashing
 *  - Account activation/deactivation (soft delete)
 *  - Failed login attempt tracking + auto-lockout
 *  - Login history (count + last login timestamp)
 */
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Column(unique = true)  // Enforce unique emails at the DB level
    private String emailId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // Accept on POST, hide on GET
    private String password;

    private boolean ifActive;

    // ==================== LOGIN TRACKING ==================== //

    private int loginCount;              // How many successful logins
    private LocalDateTime lastLogin;     // When they last logged in

    // ==================== LOCKOUT FIELDS ==================== //

    private int failedAttempts;          // Consecutive failed login attempts
    private LocalDateTime lockoutUntil;  // Null = not locked, otherwise locked until this time

    private static final int MAX_FAILED_ATTEMPTS = 5;          // Lock after 5 fails
    private static final int LOCKOUT_MINUTES = 15;             // Lock for 15 minutes

    // ==================== ONE-TO-ONE RELATIONSHIP ==================== //

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "laptop_id")
    private Laptop laptop;

    // ==================== CONSTRUCTORS ==================== //

    public User(String name, String emailId, String password) {
        this.name = name;
        this.emailId = emailId;
        this.password = password;
        this.ifActive = true;
        this.loginCount = 0;
        this.failedAttempts = 0;
    }

    public User() {
    }

    // ==================== LOCKOUT LOGIC ==================== //

    /**
     * Check if the account is currently locked out.
     * If the lockout period has expired, auto-unlock.
     */
    public boolean isLockedOut() {
        if (lockoutUntil == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(lockoutUntil)) {
            // Lockout expired - reset
            lockoutUntil = null;
            failedAttempts = 0;
            return false;
        }
        return true;
    }

    /**
     * Record a failed login attempt.
     * If max attempts reached, lock the account.
     */
    public void recordFailedAttempt() {
        this.failedAttempts++;
        if (this.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            this.lockoutUntil = LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES);
        }
    }

    /**
     * Record a successful login.
     * Resets failed attempts and updates login tracking.
     */
    public void recordSuccessfulLogin() {
        this.failedAttempts = 0;
        this.lockoutUntil = null;
        this.loginCount++;
        this.lastLogin = LocalDateTime.now();
    }

    // ==================== GETTERS AND SETTERS ==================== //

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getIsActive() {
        return ifActive;
    }

    public void setIfActive(boolean ifActive) {
        this.ifActive = ifActive;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getLockoutUntil() {
        return lockoutUntil;
    }

    public void setLockoutUntil(LocalDateTime lockoutUntil) {
        this.lockoutUntil = lockoutUntil;
    }

    public Laptop getLaptop() {
        return laptop;
    }

    public void setLaptop(Laptop laptop) {
        this.laptop = laptop;
    }
}