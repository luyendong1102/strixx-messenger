package net.ldst.chatchik.repositories;

import net.ldst.chatchik.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where u.key = ?1")
    public Optional<User> findByUserName(String username);

}
