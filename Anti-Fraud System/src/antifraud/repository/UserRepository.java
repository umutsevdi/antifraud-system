package antifraud.repository;


import antifraud.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);

    User findUserByUsernameIgnoreCase(String username);

    Boolean existsByUsername(String username);
}
