package com.example.chatify.chat.controller;

import com.example.chatify.chat.Enum.Status;
import com.example.chatify.chat.model.FriendRequest;
import com.example.chatify.chat.model.User;
import com.example.chatify.chat.repository.FriendRequestRepository;
import com.example.chatify.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api")
public class FriendsController {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/friend-requests/{username}")
    public ResponseEntity<?> sendRequest(@PathVariable String username, @AuthenticationPrincipal User sender)
    {
        User recipient =userRepository.findByUsername(username);

        FriendRequest fr=FriendRequest.builder()
                .sender(sender)
                .recipient(recipient)
                .status(Status.PENDING)
                .build();

        return ResponseEntity.ok(friendRequestRepository.save(fr));
    }



}