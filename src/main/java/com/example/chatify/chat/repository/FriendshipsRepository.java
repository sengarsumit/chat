package com.example.chatify.chat.repository;

import com.example.chatify.chat.model.Friendships;
import com.example.chatify.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipsRepository  extends JpaRepository<Friendships,Long> {
    @Query("""
        select (count(f) > 0) from Friendships f
         where (f.userLow = :a and f.userHigh = :b)
            or (f.userLow = :b and f.userHigh = :a)
    """)
    boolean existsBetween(@Param("a") User a, @Param("b") User b); // existence query pattern

    // Fetch all friendship rows where a user participates
    @Query("""
        select f from Friendships f
         where f.userLow = :u or f.userHigh = :u
    """)
    List<Friendships> findAllForUser(@Param("u") User u);
    // Find all friendships where the user is either low or high
    List<Friendships> findByUserLowOrUserHigh(User userLow, User userHigh);

    // Check if two users are already friends
    boolean existsByUserLowAndUserHigh(User userLow, User userHigh);
}
