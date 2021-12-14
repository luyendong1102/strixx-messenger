package net.ldst.chatchik.configurations;

import lombok.extern.slf4j.Slf4j;
import net.ldst.chatchik.entities.User;
import net.ldst.chatchik.repositories.RoomRepository;
import net.ldst.chatchik.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.HashSet;
import java.util.Optional;

@WebListener
@Component
@Slf4j
public class SessionListener implements HttpSessionListener {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoomRepository roomRepository;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSessionListener.super.sessionCreated(se);
    }


    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        User u = (User) se.getSession().getAttribute("userinfor");
        if (u == null) {
            HttpSessionListener.super.sessionDestroyed(se);
        }
        User user = userRepository.findByUserName(u.getKey()).get();
        if (user.getRooms() != null) {
            user.getRooms().forEach(
                    r -> {
                        r.getMembers().remove(user);
                        user.getRooms().remove(r);
                        roomRepository.save(r);
                    }
            );
        }
        if (user.getOwnroom() != null) {
            roomRepository.delete(user.getOwnroom());
        }
        userRepository.delete(user);
        HttpSessionListener.super.sessionDestroyed(se);
    }
}
