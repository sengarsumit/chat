package com.example.chatify.chat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="friendships",
uniqueConstraints ={
    @UniqueConstraint(columnNames ={"user_low_id","user_high_id"} )
        })

public class Friendships {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name="user_low_id")
    private User userLow;

    @ManyToOne(optional = false) @JoinColumn(name="user_high_id")
    private User userHigh;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public static Friendships of(User a, User b)
    {
        Friendships f=new Friendships();
        if(a.getId().compareTo(b.getId())<=0)
        {
            f.userLow=a;
            f.userHigh=b;
        }
        else {
            f.userLow=b;
            f.userHigh=a;
        }
        return f;

    }



}
