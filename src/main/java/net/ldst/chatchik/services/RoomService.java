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
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    public Room createRoom (User owner, Room room) {
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

    public Room AddMember (User user, Room room) throws ExceedMemberException {
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

    public Room RemoveUser (User user, Room room) throws ExceedMemberException {

        room.getMembers().forEach(
                u -> {
                    if (u.getKey().equals(user.getKey())) {
                        room.getMembers().remove(u);
                    }
                }
        );
        return roomRepository.save(room);
    }

}
