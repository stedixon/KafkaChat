package com.testapp.service;

import com.testapp.domain.dto.ChatRoomDTO;
import com.testapp.domain.ChatRoomDetails;
import com.testapp.domain.RoomManagement;
import com.testapp.domain.dto.UserDTO;
import com.testapp.exceptions.UserExistsException;
import com.testapp.repository.ChatRoomRepository;
import com.testapp.repository.RoomManagementRepository;
import com.testapp.repository.UserRepository;
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
    private final UserRepository userRepository;

    public ChatRoomDetails getChatRoom(String id) {
        Optional<ChatRoomDTO> chatRoom = chatRoomRepository.findById(id);
        if (chatRoom.isPresent()) {
            int participantCount = roomManagementRepository.findParticipantCountByChatRoomId(id);
            return createRoomDetails(chatRoom.get(), participantCount);
        }
        return null;
    }

    public List<UserDTO> getChatParticipants(String id) {
        List<RoomManagement> userList = roomManagementRepository.findByChatRoomId(id);

        return userList.stream()
                .map(RoomManagement::getUser)
                .collect(Collectors.toList());
    }

    public ChatRoomDTO createChatRoom(ChatRoomDTO chatRoomDTO) {
        if (Strings.isEmpty(chatRoomDTO.getDisplayName())) {
            throw new RuntimeException("Display name cannot be empty");
        }
        Optional<ChatRoomDTO> existing = chatRoomRepository.findByDisplayName(chatRoomDTO.getDisplayName());
        if (existing.isPresent()) {
            throw new UserExistsException("Chat Room " + chatRoomDTO.getDisplayName() + " is already taken.");
        }

        chatRoomDTO.setId(UUID.randomUUID().toString());
        ChatRoomDTO room = chatRoomRepository.save(chatRoomDTO);
        roomManagementRepository.save(RoomManagement.builder()
                .id(new RoomManagement.RoomManagementId(room.getAdmin().getId(), room.getId()))
                .chatRoom(room)
                .user(room.getAdmin())
                .isAdmin(true)
                .build());
        return room;
    }

    public RoomManagement addUserToRoom(ChatRoomDTO chatRoomDTO, UserDTO user) {
        // Validate that the chat room exists
        Optional<ChatRoomDTO> roomOptional = chatRoomRepository.findById(chatRoomDTO.getId());
        if (roomOptional.isEmpty()) {
            throw new RuntimeException("Chat room with id " + chatRoomDTO.getId() + " does not exist");
        }
        ChatRoomDTO room = roomOptional.get();

        // Validate that the user exists
        Optional<UserDTO> userOptional = userRepository.findById(user.getId());
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User with id " + user.getId() + " does not exist");
        }
        UserDTO userEntity = userOptional.get();

        // Check if user is already in the room
        Optional<RoomManagement> existing = roomManagementRepository.findByUserIdAndChatRoomId(user.getId(), chatRoomDTO.getId());
        if (existing.isPresent()) {
            throw new UserExistsException("User " + user.getId() + " is already in chat room " + chatRoomDTO.getId());
        }

        // Create RoomManagement with proper embedded ID
        RoomManagement roomManagement = RoomManagement.builder()
                .id(new RoomManagement.RoomManagementId(user.getId(), room.getId()))
                .user(userEntity)
                .chatRoom(room)
                .isAdmin(false)
                .build();

        return roomManagementRepository.save(roomManagement);
    }

    public List<ChatRoomDetails> getChatRoomsForUser(String userId) {
        List<RoomManagement> roomManagements = roomManagementRepository.findByUserId(userId);
        return roomManagements.stream()
                .map(rm -> {
                    int participantCount = roomManagementRepository.findParticipantCountByChatRoomId(rm.getChatRoom().getId());
                    return createRoomDetails(rm.getChatRoom(), participantCount);
                })
                .collect(Collectors.toList());
    }

    private ChatRoomDetails createRoomDetails(ChatRoomDTO room, int participantCount) {
        return ChatRoomDetails.builder()
                .id(room.getId())
                .admin(room.getAdmin())
                .displayName(room.getDisplayName())
                .description(room.getDescription())
                .participantCount(participantCount)
                .build();
    }
}
