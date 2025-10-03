package com.example.chatify.chat.repository;

import com.example.chatify.chat.Enum.Status;
import com.example.chatify.chat.model.friendRequest;
import com.example.chatify.chat.model.users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface FriendRequestRepository extends JpaRepository<friendRequest,Long>{
    boolean existsBySenderAndRecipientAndStatus(users sender,users recipient,Status status);
    Optional<friendRequest> findBySenderAndRecipientAndStatus(users sender,users recipient,Status status);
    List<friendRequest> findALLByRecipientAndStatus(users recipient,Status status);
    List<friendRequest> findAllBySenderAndStatus(users sender,Status status);
}
