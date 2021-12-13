package net.ldst.chatchik.services;

import net.ldst.chatchik.entities.User;
import net.ldst.chatchik.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${secret.pass}")
    private String defaultPass;

    public User register (String username) {
        User user = new User();
        user.setUsername(username);
        user.setKey(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(defaultPass));
        return userRepository.save(user);
    }
}
