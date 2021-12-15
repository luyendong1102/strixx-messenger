package net.ldst.chatchik.Interceptor;

import net.ldst.chatchik.entities.User;
import net.ldst.chatchik.entities.UserSecure;
import net.ldst.chatchik.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.support.AbstractHandshakeHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Component
public class UserHandshakeHandler extends AbstractHandshakeHandler {


    // event happened when http handshake, add user information to User of websocket
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        UserSecure us = (UserSecure) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new Principal() {
            @Override
            public String getName() {
                return us.getUsername();
            }
        };
    }
}
