package onetoone;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import onetoone.Laptops.Laptop;
import onetoone.Laptops.LaptopRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;

@SpringBootApplication
@EnableJpaRepositories
class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner initUser(UserRepository userRepository, LaptopRepository laptopRepository) {
        return args -> {
            // Only seed if database is empty (prevents duplicates on restart)
            if (userRepository.count() > 0) {
                System.out.println("=== Database already has data, skipping seed ===");
                return;
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            // Passwords are hashed before saving
            User user1 = new User("John", "john@somemail.com", encoder.encode("password123"));
            User user2 = new User("Jane", "jane@somemail.com", encoder.encode("janepass456"));
            User user3 = new User("Justin", "justin@somemail.com", encoder.encode("justinpass789"));

            Laptop laptop1 = new Laptop(2.5, 4, 8, "Lenovo", 300);
            Laptop laptop2 = new Laptop(4.1, 8, 16, "Hp", 800);
            Laptop laptop3 = new Laptop(3.5, 32, 32, "Dell", 2300);

            user1.setLaptop(laptop1);
            user2.setLaptop(laptop2);
            user3.setLaptop(laptop3);

            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);

            System.out.println("===========================================");
            System.out.println("  Test accounts seeded (passwords hashed)!");
            System.out.println("  Login with POST /login using:");
            System.out.println("    john@somemail.com / password123");
            System.out.println("    jane@somemail.com / janepass456");
            System.out.println("    justin@somemail.com / justinpass789");
            System.out.println("===========================================");
        };
    }
}