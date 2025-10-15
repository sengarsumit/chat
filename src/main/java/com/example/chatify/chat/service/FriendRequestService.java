package com.example.chatify.chat.service;

import com.example.chatify.chat.Enum.Status;
import com.example.chatify.chat.model.FriendRequest;
import com.example.chatify.chat.model.Friendships;
import com.example.chatify.chat.model.User;
import com.example.chatify.chat.repository.FriendRequestRepository;
import com.example.chatify.chat.repository.FriendshipsRepository;
import com.example.chatify.chat.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipsRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * Send a new friend request
     */
    @Transactional
    public FriendRequest sendRequest(UUID senderId, String recipientUsername) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User recipient = userRepository.findByUsername(recipientUsername);
        if(recipient == null) {
            throw new RuntimeException("Recipient not found");
        }

        // Prevent self-requests
        if (sender.getId().equals(recipient.getId())) {
            throw new RuntimeException("You cannot send a request to yourself");
        }

        // Check if request already exists
        friendRequestRepository.findBySenderAndRecipient(sender, recipient)
                .ifPresent(r -> {
                    throw new RuntimeException("Request already sent");
                });

        // Check if already friends
        if (friendshipRepository.existsByUserLowAndUserHigh(sender, recipient)) {
            throw new RuntimeException("Users are already friends");
        }

        FriendRequest request=FriendRequest.builder()
                .sender(sender)
                .recipient(recipient)
                .status(Status.PENDING)
                .build();
        return friendRequestRepository.save(request);
    }

    /**
     * Accept friend request
     */
    @Transactional
    public Friendships acceptRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(Status.ACCEPTED);
        friendRequestRepository.save(request);

        // Create normalized friendship
        Friendships friendship = Friendships.of(request.getSender(), request.getRecipient());
        return friendshipRepository.save(friendship);
    }

    /**
     * Reject friend request
     */
    @Transactional
    public void rejectRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(Status.REJECTED);
        friendRequestRepository.save(request);
    }

    /**
     * Get pending requests for a user
     */
    public List<FriendRequest> getPendingRequests(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return friendRequestRepository.findByRecipientAndStatus(user, Status.PENDING);
    }

    /**
     * Get friends list
     */
    public List<Friendships> getFriends(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return friendshipRepository.findByUserLowOrUserHigh(user, user);
    }
}
