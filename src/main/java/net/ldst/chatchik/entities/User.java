package net.ldst.chatchik.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@AllArgsConstructor
@Getter
@Setter
@ToString
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "key", unique = true)
    private String key;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @OneToOne(mappedBy = "owner")
    private Room ownroom;

    @ManyToMany(mappedBy = "members", fetch = FetchType.EAGER)
    @ToString.Exclude
    private Set<Room> rooms;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roomwaitingid")
    private Room waiting;

}
