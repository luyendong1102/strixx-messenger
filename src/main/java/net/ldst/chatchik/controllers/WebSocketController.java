package net.ldst.chatchik.controllers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.ldst.chatchik.entities.ChatMessage;
import net.ldst.chatchik.entities.Room;
import net.ldst.chatchik.entities.User;
import net.ldst.chatchik.exceptions.ExceedMemberException;
import net.ldst.chatchik.repositories.RoomRepository;
import net.ldst.chatchik.repositories.UserRepository;
import net.ldst.chatchik.services.EncrypMessageService;
import net.ldst.chatchik.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;

@Controller
@Slf4j
public class WebSocketController {

    @Autowired
    private SimpMessageSendingOperations operator;

    @Autowired
    private EncrypMessageService encrypMessageService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/test")
    public void test(SimpMessageHeaderAccessor accessor) {
        log.info(accessor.getUser().getName() + " connected");
        operator.convertAndSendToUser(accessor.getUser().getName(), "/msg/test", "hello this is test message");
    }

    // production here

    @SneakyThrows
    @MessageMapping("/send.chat")
    public void testsend(@Payload ChatMessage msg, SimpMessageHeaderAccessor accessor) {
        String roomid = msg.getRoomid();
        String userid = accessor.getUser().getName();
        Room room = roomRepository.findByRoomId(roomid).get();

//        log.info("current " + userid);
//        if (room.getMembers().stream().noneMatch(
//                u -> {
//                    log.info(u.getKey());
//                    return u.getKey().equals(userid);
//                }
//        )) {
//            log.info("not current user ");
//            return;
//        }

        if (room.getMembers().stream().noneMatch(
                uus -> {
                    return uus.getKey().equals(userid);
                }
        )) {
            log.info("wrong address ");
            return;
        }

        String context = encrypMessageService.CBCDecrypter(userid, msg.getContent());
        msg.setUserid(DigestUtils.md5DigestAsHex(userid.getBytes(StandardCharsets.UTF_8)));
        room.getMembers().forEach(
                user -> {
                    String key = user.getKey();
                    String newEcrypt = encrypMessageService.CBCEncrypter(key, context);
                    msg.setContent(newEcrypt);
                    operator.convertAndSendToUser(user.getKey(), "/msg/" + roomid, msg);
                }
        );
    }

    @MessageMapping("/send.entrypoint")
    public void testAddM(@Payload ChatMessage msg, SimpMessageHeaderAccessor accessor) throws ExceedMemberException {
        String roomid = msg.getRoomid();
        User u = (User) accessor.getSessionAttributes().get("userinfor");
        Optional<Room> rr = roomRepository.findByRoomId(roomid);

        if (rr.isEmpty()) {
            throw new ExceedMemberException();
        }

        Room r = rr.get();

        log.info("userkey " + u.getKey());
        msg.setUserid(DigestUtils.md5DigestAsHex(u.getKey().getBytes(StandardCharsets.UTF_8)));
        if (u.getOwnroom() == null) {
            u.setOwnroom(new Room());
            u.getOwnroom().setRoomid("not correct roomid");
        }

        // when user id owner
        if (u.getOwnroom().getRoomid().equals(msg.getRoomid())) {
            msg.setType(ChatMessage.MessageType.APPROVED);
            r.getMembers().forEach(
                    user -> {
                        operator.convertAndSendToUser(user.getKey(), "/msg/" + roomid, msg);
                    }
            );
            return;
        }

        // when room is max
        if (r.getMembers().size() >= r.getMax_member()) {
            msg.setType(ChatMessage.MessageType.LEAVE);
            operator.convertAndSendToUser(u.getKey(), "/msg/" + roomid, msg);
            return;
        }

        // when room is locked
        if (r.getIs_locked()) {
            msg.setType(ChatMessage.MessageType.PENDDING);
            operator.convertAndSendToUser(u.getKey(), "/msg/" + roomid, msg);
            String ownerkey = r.getOwner().getKey();
            r.getMembers().forEach(
                    user -> {
                        if (user.getKey().equals(u.getKey()) || user.getKey().equals(ownerkey)) {
                            return;
                        }
                        operator.convertAndSendToUser(user.getKey(), "/msg/" + roomid, msg);
                    }
            );

            // user key in content
            msg.setType(ChatMessage.MessageType.NEEDPERM);
            msg.setAuthor(u.getUsername());
            msg.setContent(u.getKey());
            r = roomService.AddPendingList(u, r);
            operator.convertAndSendToUser(r.getOwner().getKey(), "/msg/" + roomid, msg);

            return;
        }

        // when everythings fine
        roomService.AddMember(u, r);
        msg.setType(ChatMessage.MessageType.APPROVED);
        r.getMembers().forEach(
                user -> {
                    operator.convertAndSendToUser(user.getKey(), "/msg/" + roomid, msg);
                }
        );
    }

    @MessageMapping("/send.command")
    public void testCmd(@Payload ChatMessage msg, SimpMessageHeaderAccessor accessor) throws ExceedMemberException {
        String roomid = msg.getRoomid();
        User u = (User) accessor.getSessionAttributes().get("userinfor");
        Room r = roomRepository.findByRoomId(roomid).get();
        if (u.getOwnroom() == null) {
            u.setOwnroom(new Room());
            u.getOwnroom().setRoomid("not correct roomid");
        }

        // leave
        msg.setUserid(DigestUtils.md5DigestAsHex(u.getKey().getBytes(StandardCharsets.UTF_8)));
        if (msg.getType().equals(ChatMessage.MessageType.LEAVE)) {

            r = roomService.RemoveUser(u, r);

            if (r.getMembers().size() == 0) {
                roomRepository.delete(r);
                return;
            }

            if (u.getKey().equals(r.getOwner().getKey())) {
                r.setOwner(r.getMembers().iterator().next());
                log.info(roomRepository.save(r).getOwner().getUsername());
                u.setOwnroom(null);
                msg.setType(ChatMessage.MessageType.NEWLEAD);
                operator.convertAndSendToUser(r.getMembers().iterator().next().getKey(), "/msg/" + roomid, msg);
            }

            msg.setType(ChatMessage.MessageType.LEAVE);
            r.getMembers().forEach(
                    user -> {
                        operator.convertAndSendToUser(user.getKey(), "/msg/" + roomid, msg);
                    }
            );
            return;
        }

        // lock
        if (msg.getType().equals(ChatMessage.MessageType.LOCK)) {
            if (r.getOwner().getKey().equals(u.getKey())) {
                r.setIs_locked(true);
                roomRepository.save(r);
                r.getMembers().forEach(
                        user -> {
                            operator.convertAndSendToUser(user.getKey(), "/msg/" + roomid, msg);
                        }
                );
                return;
            }
            return;
        }

        // unlock
        if (msg.getType().equals(ChatMessage.MessageType.UNLOCK)) {
            if (u.getOwnroom().getRoomid().equals(r.getRoomid())) {
                r.setIs_locked(false);
                roomRepository.save(r);
                r.getMembers().forEach(
                        user -> {
                            operator.convertAndSendToUser(user.getKey(), "/msg/" + roomid, msg);
                        }
                );
                return;
            }
            return;
        }

        // current member
        if (msg.getType().equals(ChatMessage.MessageType.CURMEM)) {
            r.getMembers().forEach(
                    user -> {
                        msg.setAuthor(user.getUsername());
                        operator.convertAndSendToUser(u.getKey(), "/msg/" + roomid, msg);
                    }
            );
            return;
        }

        // approve user to room
        if (msg.getType().equals(ChatMessage.MessageType.APPROVE)) {
            if (r.getOwner().getKey().equals(u.getKey())) {
                User user = userRepository.findByUserName(msg.getContent()).get();
                if (r.getMembers().size() >= r.getMax_member()) {
                    return;
                }
                r = roomService.AddFromWaitingList(user, r);
                msg.setType(ChatMessage.MessageType.APPROVED);
                r.getMembers().forEach(
                        userr -> {
                            operator.convertAndSendToUser(userr.getKey(), "/msg/" + roomid, msg);
                        }
                );
            }
        }

        // todo
        if (msg.getType().equals(ChatMessage.MessageType.NOTAPV)) {
            if (r.getOwner().getKey().equals(u.getKey())) {
                Room finalR = r;
                r.getWaiting().forEach(
                        uuser -> {
                            if (uuser.getKey().equals(msg.getContent())) {
                                finalR.getWaiting().remove(uuser);
                            }
                        }
                );
                roomRepository.save(finalR);
                msg.setType(ChatMessage.MessageType.NOTAPV);
                operator.convertAndSendToUser(msg.getContent(), "/msg/" + roomid, msg);
            }
        }

    }

}
