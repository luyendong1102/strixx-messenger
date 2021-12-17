package net.ldst.chatchik.controllers;

import lombok.extern.slf4j.Slf4j;
import net.ldst.chatchik.entities.Room;
import net.ldst.chatchik.entities.User;
import net.ldst.chatchik.exceptions.GeneralException;
import net.ldst.chatchik.repositories.RoomRepository;
import net.ldst.chatchik.repositories.UserRepository;
import net.ldst.chatchik.services.RoomService;
import net.ldst.chatchik.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.swing.text.html.Option;
import java.util.Optional;

@Controller
@Slf4j
public class PageController {

    @Autowired
    private UserServices userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${secret.pass}")
    private String defaultpass;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    private void authencate(User u, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                u.getKey(), defaultpass
        );
        Authentication auuth = authenticationManager.authenticate(auth);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(auth);
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        session.setAttribute("userinfo", u);
    }


    @GetMapping("/")
    public String homepage () {
        return "index";
    }

    @PostMapping("/register")
    public String registerpage (HttpServletRequest request, @RequestParam(defaultValue = "anonymous")String username) {
        User u = userService.register(username);
        try {
            authencate(u, request);
            return "redirect:/greeting";
        }
        catch (Exception e) {
            userRepository.delete(u);
            return "redirect:/";
        }
    }

    @GetMapping("/greeting")
    public String greetingpage (HttpServletRequest request, Model model) {
        User u = (User) request.getSession().getAttribute("userinfo");
        model.addAttribute("username", u.getUsername());
        return "welcome";
    }

    @PostMapping("/createroom")
    public String createroompage (HttpServletRequest request, @RequestParam(defaultValue = "4")Integer rom_max_member, @RequestParam(defaultValue = "0")Boolean isLocked) throws GeneralException {
        User u = (User) request.getSession().getAttribute("userinfo");
        Room newRoom = new Room();
        newRoom.setMax_member(rom_max_member);
        newRoom.setIs_locked(isLocked);
        newRoom = roomService.createRoom(u, newRoom);
        return "redirect:/chat/" + newRoom.getRoomid();
    }

    // todo secure problem
    @GetMapping("/chat/{roomid}")
    public String chatroompage (HttpServletRequest request, @PathVariable String roomid, Model model) {

        Optional<Room> room = roomRepository.findByRoomId(roomid);
        if (room.isEmpty()) {
            return "redirect:/greeting";
        }

        User u = (User) request.getSession().getAttribute("userinfo");
        model.addAttribute("userid", u.getKey());
        model.addAttribute("username", u.getUsername());
        model.addAttribute("roomid", roomid);
        model.addAttribute("globalkey", defaultpass);
        return "chat";
    }
    
    @GetMapping("/invite/{roomid}")
    public String invitetepage (HttpServletRequest request, @PathVariable String roomid, Model model) {
        User u = (User) request.getSession().getAttribute("userinfo");
        Optional<Room> room = roomRepository.findByRoomId(roomid);
        if (room.isEmpty()) {
            if (u != null) {
                return "redirect:/greeting";
            }
            return "redirect:/";
        }
        if (u == null) {
            log.info("new anonymous user click invite");
            return "redirect:/prejoin/" + roomid;
        }
        return "redirect:/chat/" + roomid;
    }
    
    @PostMapping("/invite")
    public String handleinvite (HttpServletRequest request, @RequestParam String key, Model model) {
        Optional<Room> room = roomRepository.findByRoomId(key);
        if (room.isEmpty()) {
            return "redirect:/greeting";
        }
        return "redirect:/chat/" + key;
    }

    @GetMapping("/prejoin/{roomid}")
    public String prejoin (HttpServletRequest request, @PathVariable String roomid,Model model) {
        model.addAttribute("roomid", roomid);
        return "portableRegis";
    }
    
    @PostMapping("/prejoin/{roomid}")
    public String registerNewAnonoymousUser (HttpServletRequest request, @RequestParam(defaultValue = "anonymous")String username, @PathVariable String roomid,Model model) {
        User u = (User) request.getSession().getAttribute("userinfo");
        if (u == null) {
            log.info("new anonymous user going to register");
            u = userService.register(username);
        }
        try {
            authencate(u, request);
            return "redirect:/chat/" + roomid;
        }
        catch (Exception e) {
            userRepository.delete(u);
            return "redirect:/";
        }
    }

    @GetMapping("/error/{message}")
    public String errorHandler (@PathVariable String error, Model model) {
        model.addAttribute("error", error);
        return "error";
    }

}
