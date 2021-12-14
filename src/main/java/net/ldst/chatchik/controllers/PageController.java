package net.ldst.chatchik.controllers;

import lombok.extern.slf4j.Slf4j;
import net.ldst.chatchik.entities.Room;
import net.ldst.chatchik.entities.User;
import net.ldst.chatchik.exceptions.ExceedMemberException;
import net.ldst.chatchik.repositories.RoomRepository;
import net.ldst.chatchik.repositories.UserRepository;
import net.ldst.chatchik.services.RoomService;
import net.ldst.chatchik.services.UserSecureService;
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

@Controller
@Slf4j
public class PageController {

    @Autowired
    private UserServices userService;

    @Autowired
    private UserSecureService userSecureServiceSecure;

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
        session.setAttribute("userinfor", u);
    }


    @GetMapping("/")
    public String iindex () {
        return "index";
    }

    @PostMapping("/register")
    public String regiss (HttpServletRequest request, @RequestParam(defaultValue = "anonymous")String username) {
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
    public String wwelcome (HttpServletRequest request, Model model) {
        User u = (User) request.getSession().getAttribute("userinfor");
        model.addAttribute("username", u.getUsername());
        return "welcome";
    }

    @PostMapping("/createroom")
    public String createeRoom (HttpServletRequest request, @RequestParam(defaultValue = "4")Integer rom_max_member, @RequestParam(defaultValue = "0")Boolean isLocked) {
        User u = (User) request.getSession().getAttribute("userinfor");
        Room newRoom = new Room();
        newRoom.setMax_member(rom_max_member);
        newRoom.setIs_locked(isLocked);
        newRoom = roomService.createRoom(u, newRoom);
        request.getSession().setAttribute("userinfor", userRepository.findByUserName(u.getKey()).get());
        return "redirect:/chat/" + newRoom.getRoomid();
    }

    // todo secure problem
    @GetMapping("/chat/{roomid}")
    public String joinChat (HttpServletRequest request, @PathVariable String roomid, Model model) {
        User u = (User) request.getSession().getAttribute("userinfor");
        model.addAttribute("userid", u.getKey());
        model.addAttribute("username", u.getUsername());
        model.addAttribute("roomid", roomid);
        model.addAttribute("globalkey", defaultpass);
        return "chat";
    }

    // todo in service
    @GetMapping("/invite/{roomid}")
    public String prePareChat (HttpServletRequest request, @PathVariable String roomid, Model model) {
        User u = (User) request.getSession().getAttribute("userinfor");
        if (u == null) {
            log.info("new user");
            return "redirect:/prejoin/" + roomid;
        }
        return "redirect:/chat/" + roomid;
    }

    // todo in service
    @PostMapping("/invite")
    public String JJoinChat (HttpServletRequest request, @RequestParam String key, Model model) {
        return "redirect:/chat/" + key;
    }

    @GetMapping("/prejoin/{roomid}")
    public String preJoiChatUI (HttpServletRequest request, @PathVariable String roomid,Model model) {
        model.addAttribute("roomid", roomid);
        return "portableRegis";
    }

    // todo in service
    @PostMapping("/prejoin/{roomid}")
    public String preJoiChat (HttpServletRequest request, @RequestParam(defaultValue = "anonymous")String username, @PathVariable String roomid,Model model) {
        User u = userService.register(username);
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
    public String errorHandler (@PathVariable String error) {
        return "error";
    }

}
