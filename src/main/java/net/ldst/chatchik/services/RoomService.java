package net.ldst.chatchik.services;

import lombok.extern.slf4j.Slf4j;
import net.ldst.chatchik.entities.Room;
import net.ldst.chatchik.entities.User;
import net.ldst.chatchik.exceptions.GeneralException;
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

    public Room createRoom (User oowner, Room room) throws GeneralException {
        Optional<User> resultUser = userRepository.findByUserName(oowner.getKey());
        if (resultUser.isEmpty()) {
            throw new GeneralException("Owner User not found");
        }
        User owner = resultUser.get();
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

    public Room AddMember (User uuser, Room rroom) throws GeneralException {
        Optional<User> resultUser = userRepository.findByUserName(uuser.getKey());
        Optional<Room> resultRoom = roomRepository.findByRoomId(rroom.getRoomid());
        if (resultRoom.isEmpty()) {
            throw new GeneralException("Room not found");
        }
        if (resultUser.isEmpty()) {
            throw new GeneralException("User not found");
        }

        Room room = resultRoom.get();
        User user = resultUser.get();

        if (room.getMembers().size() == room.getMax_member()) {
            throw new GeneralException("Room is max");
        }
        if (user.getRooms() == null) {
            user.setRooms(new HashSet<>());
            user.getRooms().add(room);
        }
        user.getRooms().add(room);
        room.getMembers().add(user);
        return roomRepository.save(room);
    }

    public Room RemoveUser (User uuser, Room rroom) throws GeneralException {
        Optional<User> resultUser = userRepository.findByUserName(uuser.getKey());
        Optional<Room> resultRoom = roomRepository.findByRoomId(rroom.getRoomid());
        if (resultRoom.isEmpty()) {
            throw new GeneralException("Room not found");
        }
        if (resultUser.isEmpty()) {
            throw new GeneralException("User not found");
        }

        Room room = resultRoom.get();
        User user = resultUser.get();

        room.getMembers().removeIf(u -> u.getKey().equals(user.getKey()));

        return roomRepository.save(room);
    }

    public Room AddPendingList (User user, Room room) throws GeneralException {
        Optional<User> resultUser = userRepository.findByUserName(user.getKey());
        Optional<Room> resultRoom = roomRepository.findByRoomId(room.getRoomid());
        if (resultRoom.isEmpty()) {
            throw new GeneralException("Room not found");
        }
        if (resultUser.isEmpty()) {
            throw new GeneralException("User not found");
        }

        Room r = resultRoom.get();
        User u = resultUser.get();

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

    public Room AddFromWaitingList (User uuser, Room r) throws GeneralException {
        Optional<User> resultUser = userRepository.findByUserName(uuser.getKey());
        Optional<Room> resultRoom = roomRepository.findByRoomId(r.getRoomid());
        if (resultRoom.isEmpty()) {
            throw new GeneralException("Room not found");
        }
        if (resultUser.isEmpty()) {
            throw new GeneralException("User not found");
        }

        Room room = resultRoom.get();
        User user = resultUser.get();

        if (room.getWaiting() == null) {
            room.setWaiting(new HashSet<>());
        }
        if (room.getWaiting() == null) {
            return AddMember(user, room);
        }

        room.getWaiting().removeIf(uindex -> uindex.getKey().equals(user.getKey()));
        if (room.getMembers().size() >= room.getMax_member()) {
            return room;
        }
        room.getMembers().add(user);
        return roomRepository.save(room);
    }

}
