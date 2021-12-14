package net.ldst.chatchik.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "roomid", unique = true)
    private String roomid;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "ownerid")
    private User owner;

    @Column(name = "max_member")
    private Integer max_member;

    @Column(name = "is_locked")
    private Boolean is_locked;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "userroom",
            joinColumns = @JoinColumn(name = "roomid"),
            inverseJoinColumns = @JoinColumn(name = "userid")
    )
    @ToString.Exclude
    private Set<User> members;

    @OneToMany(mappedBy = "waiting", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<User> waiting;

}
