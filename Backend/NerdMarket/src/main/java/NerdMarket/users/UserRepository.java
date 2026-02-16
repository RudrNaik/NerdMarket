package NerdMarket.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByUsername(String username);
    Users findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Users findUsersById(Long id);
}
