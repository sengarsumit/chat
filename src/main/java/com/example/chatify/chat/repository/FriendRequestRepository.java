package com.example.chatify.chat.repository;

import com.example.chatify.chat.Enum.Status;
import com.example.chatify.chat.model.FriendRequest;
import com.example.chatify.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public interface FriendRequestRepository extends JpaRepository<FriendRequest,Long>{
    boolean existsBySenderAndRecipientAndStatus(User sender, User recipient, Status status);
    Optional<FriendRequest> findBySenderAndRecipientAndStatus(User sender, User recipient, Status status);
    List<FriendRequest> findALLByRecipientAndStatus(User recipient, Status status);
    List<FriendRequest> findAllBySenderAndStatus(User sender, Status status);

    Arrays findAllByRecipientAndStatus(User recipient, Status status);
    // All requests sent by a user
    List<FriendRequest> findBySender(User sender);

    // All requests received by a user
    List<FriendRequest> findByRecipient(User recipient);

    // Pending requests received by a user
    List<FriendRequest> findByRecipientAndStatus(User recipient, Status status);

    // Check if request already exists
    Optional<FriendRequest> findBySenderAndRecipient(User sender, User recipient);
}
