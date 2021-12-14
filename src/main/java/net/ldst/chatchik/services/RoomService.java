package net.ldst.chatchik.services;

import lombok.extern.slf4j.Slf4j;
import net.ldst.chatchik.entities.Room;
import net.ldst.chatchik.entities.User;
import net.ldst.chatchik.exceptions.ExceedMemberException;
import net.ldst.chatchik.repositories.RoomRepository;
import net.ldst.chatchik.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    public Room createRoom (User oowner, Room room) {
        User owner = userRepository.findByUserName(oowner.getKey()).get();
        room.setRoomid(UUID.randomUUID().toString());
        room.setOwner(owner);
        room.setMembers(new HashSet<>());
        if (owner.getRooms() == null) {
            owner.setRooms(new HashSet<>());
            owner.getRooms().add(room);
        }
        owner.getRooms().add(room);
        room.getMembers().add(owner);
        return roomRepository.save(room);
    }

    public Optional<Room> findByRoomID(String idd) {
        return roomRepository.findByRoomId(idd);
    }

    public Room AddMember (User uuser, Room rroom) throws ExceedMemberException {
        User user = userRepository.findByUserName(uuser.getKey()).get();
        Room room = roomRepository.findByRoomId(rroom.getRoomid()).get();
        if (room.getMembers().size() == room.getMax_member()) {
            throw new ExceedMemberException();
        }
        if (user.getRooms() == null) {
            user.setRooms(new HashSet<>());
            user.getRooms().add(room);
        }
        user.getRooms().add(room);
        room.getMembers().add(user);
        return roomRepository.save(room);
    }

    public Room RemoveUser (User uuser, Room rroom) throws ExceedMemberException {
        User user = userRepository.findByUserName(uuser.getKey()).get();
        Room room = roomRepository.findByRoomId(rroom.getRoomid()).get();
        room.getMembers().removeIf(u -> u.getKey().equals(user.getKey()));
        return roomRepository.save(room);
    }

    public Room AddPendingList (User user, Room room) {
        Room r = roomRepository.findByRoomId(room.getRoomid()).get();
        User u = userRepository.findByUserName(user.getKey()).get();
        if (r.getWaiting() == null) {
            r.setWaiting(new HashSet<>());
        }
        if (r.getWaiting().stream().anyMatch(
                uur -> {
                    return uur.getKey().equals(u.getKey());
                }
        )) {
            return room;
        }
        u.setWaiting(r);
        r.getWaiting().add(u);
        return roomRepository.save(r);
    }

    public Room AddFromWaitingList (User uuser, Room r) throws ExceedMemberException {
        Room room = roomRepository.findByRoomId(r.getRoomid()).get();
        User user = userRepository.findByUserName(uuser.getKey()).get();
        if (room.getWaiting() == null) {
            room.setWaiting(new HashSet<>());
        }
        log.info(room.getWaiting().size()+ " size");
        if (room.getWaiting() == null) {
            return AddMember(user, room);
        }

        room.getWaiting().removeIf(uindex -> uindex.getKey().equals(user.getKey()));
        if (room.getMembers().size() >= room.getMax_member()) {
            return room;
        }
        room.getMembers().add(user);

//        room.getWaiting().forEach(
//                u -> {
//                    if (u.getKey().equals(user.getKey())) {
//                        room.getWaiting().remove(u);
//                        if (room.getMembers().size() >= room.getMax_member()) {
//                            return;
//                        }
//                        room.getMembers().add(u);
//                    }
//                }
//        );
        return roomRepository.save(room);
    }

}
