package com.testapp.service;

import com.testapp.domain.ChatRoom;
import com.testapp.domain.RoomManagement;
import com.testapp.exceptions.UserExistsException;
import com.testapp.repository.ChatRoomRepository;
import com.testapp.repository.RoomManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final RoomManagementRepository roomManagementRepository;

    public ChatRoom getChatRoom(String id) {
        return chatRoomRepository.findByIdWithParticipantCount(id).orElse(null);
    }

    public ChatRoom createChatRoom(ChatRoom chatRoom) {
        Optional<ChatRoom> existing = chatRoomRepository.findByDisplayName(chatRoom.getDisplayName());
        if (existing.isPresent()) {
            throw new UserExistsException("Chat Room " + chatRoom.getDisplayName() + " is already taken.");
        }

        chatRoom.setId(UUID.randomUUID().toString());
        return chatRoomRepository.save(chatRoom);
    }

    public RoomManagement addUserToRoom(String chatRoomId, String userId) {
        Optional<RoomManagement> existing = roomManagementRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
        if (existing.isPresent()) {
            throw new UserExistsException("User " + userId + " is already in chat room " + chatRoomId);
        }

        return roomManagementRepository.save(new RoomManagement(userId, chatRoomId));
    }
}
