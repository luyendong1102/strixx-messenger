package net.ldst.chatchik.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatMessage implements Serializable {

    public enum MessageType {
        CHAT, CONNECT, LEAVE, LOCK, UNLOCK, APPROVED, APPROVE, PENDDING, CURMEM, NEEDPERM, NOTAPV;
    }

    private String author;
    private String content;
    private String roomid;
    private MessageType type;
    private String userid;

}
