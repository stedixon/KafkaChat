package com.testapp.service;

import com.testapp.domain.ChatRoom;
import com.testapp.domain.ChatRoomDetails;
import com.testapp.domain.RoomManagement;
import com.testapp.domain.User;
import com.testapp.exceptions.UserExistsException;
import com.testapp.repository.ChatRoomRepository;
import com.testapp.repository.RoomManagementRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final RoomManagementRepository roomManagementRepository;

    public ChatRoomDetails getChatRoom(String id) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(id);
        if (chatRoom.isPresent()) {
            int participantCount = roomManagementRepository.findParticipantCountByChatRoomId(id);
            return createRoomDetails(chatRoom.get(), participantCount);
        }
        return null;
    }

    public List<User> getChatParticipants(String id) {
        List<RoomManagement> userList = roomManagementRepository.findByChatRoomId(id);

        return userList.stream()
                .map(RoomManagement::getUser)
                .collect(Collectors.toList());
    }

    public ChatRoom createChatRoom(ChatRoom chatRoom) {
        if (Strings.isEmpty(chatRoom.getDisplayName())) {
            throw new RuntimeException("Display name cannot be empty");
        }
        Optional<ChatRoom> existing = chatRoomRepository.findByDisplayName(chatRoom.getDisplayName());
        if (existing.isPresent()) {
            throw new UserExistsException("Chat Room " + chatRoom.getDisplayName() + " is already taken.");
        }

        chatRoom.setId(UUID.randomUUID().toString());
        ChatRoom room = chatRoomRepository.save(chatRoom);
        roomManagementRepository.save(RoomManagement.builder()
                .id(new RoomManagement.RoomManagementId(room.getAdmin().getId(), room.getId()))
                .chatRoom(room)
                .user(room.getAdmin())
                .isAdmin(true)
                .build());
        return room;
    }

    public RoomManagement addUserToRoom(ChatRoom chatRoom, User user) {
        Optional<RoomManagement> existing = roomManagementRepository.findByUserIdAndChatRoomId(user.getId(), chatRoom.getId());
        if (existing.isPresent()) {
            throw new UserExistsException("User " + user.getId() + " is already in chat room " + chatRoom.getId());
        }

        return roomManagementRepository.save(new RoomManagement(user, chatRoom));
    }

    private ChatRoomDetails createRoomDetails(ChatRoom room, int participantCount) {
        return ChatRoomDetails.builder()
                .id(room.getId())
                .admin(room.getAdmin())
                .displayName(room.getDisplayName())
                .description(room.getDescription())
                .participantCount(participantCount)
                .build();
    }
}
