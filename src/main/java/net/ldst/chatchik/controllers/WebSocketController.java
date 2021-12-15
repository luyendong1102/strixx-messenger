package net.ldst.chatchik.controllers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.ldst.chatchik.entities.ChatMessage;
import net.ldst.chatchik.entities.Room;
import net.ldst.chatchik.entities.User;
import net.ldst.chatchik.exceptions.GeneralException;
import net.ldst.chatchik.repositories.RoomRepository;
import net.ldst.chatchik.repositories.UserRepository;
import net.ldst.chatchik.services.EncrypMessageService;
import net.ldst.chatchik.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

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
    public void chathandler(@Payload ChatMessage msg, SimpMessageHeaderAccessor accessor) {
        String roomid = msg.getRoomid();
        User u = (User) Objects.requireNonNull(accessor.getSessionAttributes()).get("userinfo");
        String userid = u.getKey();
        Room room = roomRepository.findByRoomId(roomid).get();

        // if user is not in room, send no message
        if (room.getMembers().stream().noneMatch(
                uus -> {
                    return uus.getKey().equals(userid);
                }
        )) {
            log.info("User is not in room ");
            return;
        }

        // encrypt data;
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

    // when user fisrt connect to web socket
    @MessageMapping("/send.entrypoint")
    public void entrypoint(@Payload ChatMessage msg, SimpMessageHeaderAccessor accessor) throws GeneralException {
        String roomid = msg.getRoomid();
        User u = (User) accessor.getSessionAttributes().get("userinfo");
        Optional<Room> resultRoom = roomRepository.findByRoomId(roomid);

        if (resultRoom.isEmpty()) {
            throw new GeneralException("Room not found");
        }

        if (u == null) {
            throw new GeneralException("User not found");
        }

        Room r = resultRoom.get();
        log.info("User ID: " + u.getKey() + " connected WebSocket");

        // hash user id
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

            if (r.getOwner().getKey().equals(u.getKey())) {
                roomService.AddMember(u, r);
                msg.setType(ChatMessage.MessageType.APPROVED);
                r.getMembers().forEach(
                        user -> {
                            operator.convertAndSendToUser(user.getKey(), "/msg/" + roomid, msg);
                        }
                );
                return;
            }

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

            // send join request to room admin
            msg.setType(ChatMessage.MessageType.NEEDPERM);
            msg.setAuthor(u.getUsername());
            msg.setContent(u.getKey());
            r = roomService.AddPendingList(u, r);
            operator.convertAndSendToUser(r.getOwner().getKey(), "/msg/" + roomid, msg);

            return;
        }

        // when there is no interceptor
        roomService.AddMember(u, r);
        msg.setType(ChatMessage.MessageType.APPROVED);
        r.getMembers().forEach(
                user -> {
                    operator.convertAndSendToUser(user.getKey(), "/msg/" + roomid, msg);
                }
        );
    }

    // message command (special command)
    @MessageMapping("/send.command")
    public void handlecommands(@Payload ChatMessage msg, SimpMessageHeaderAccessor accessor) throws GeneralException {
        String roomid = msg.getRoomid();
        User u = (User) accessor.getSessionAttributes().get("userinfo");
        Optional<Room> resultRoom = roomRepository.findByRoomId(roomid);

        if (resultRoom.isEmpty()) {
            throw new GeneralException("Room not found");
        }

        if (u == null) {
            throw new GeneralException("User not found");
        }

        Room r = resultRoom.get();

        // set fake ownroom to avoid null error
        if (u.getOwnroom() == null) {
            u.setOwnroom(new Room());
            u.getOwnroom().setRoomid("not correct roomid");
        }

        // hash userid
        msg.setUserid(DigestUtils.md5DigestAsHex(u.getKey().getBytes(StandardCharsets.UTF_8)));

        // leave command
        if (msg.getType().equals(ChatMessage.MessageType.LEAVE)) {

            r = roomService.RemoveUser(u, r);

            if (r.getMembers().size() == 0) {
                roomRepository.delete(r);
                log.info("Room: " + r.getRoomid() + " deleted");
                return;
            }

            // set new admin to next member, just like FIFO
            if (u.getKey().equals(r.getOwner().getKey())) {
                r.setOwner(r.getMembers().iterator().next());
                roomRepository.save(r);
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

        // lock command
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

        // unlock command
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

        // not approve user
        if (msg.getType().equals(ChatMessage.MessageType.NOTAPV)) {
            if (r.getOwner().getKey().equals(u.getKey())) {

                r.getWaiting().removeIf( uindex -> uindex.getKey().equals(msg.getContent()));

                roomRepository.save(r);
                msg.setType(ChatMessage.MessageType.NOTAPV);
                operator.convertAndSendToUser(msg.getContent(), "/msg/" + roomid, msg);
            }
        }

    }

}
