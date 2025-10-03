package com.example.chatify.chat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="friendships",
uniqueConstraints ={
    @UniqueConstraint(columnNames ={"user_low_id","user_high_id"} )
        })

public class friendships {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name="user_low_id")
    private users userLow;

    @ManyToOne(optional = false) @JoinColumn(name="user_high_id")
    private users userHigh;

    @Column(nullable = false)
    private Instant createdAt=Instant.now();

    public static friendships of(users a,users b)
    {
        friendships f=new friendships();
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
