package com.chat.app.chat_app_backend.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.chat.app.chat_app_backend.config.AppConstants;
import com.chat.app.chat_app_backend.entities.Message;
import com.chat.app.chat_app_backend.entities.Room;
import com.chat.app.chat_app_backend.playload.MessageRequest;
import com.chat.app.chat_app_backend.repositories.RoomRepository;

@Controller
@CrossOrigin(AppConstants.FRONT_END_BASE_URL)
public class ChatController {

    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(RoomRepository roomRepository, SimpMessagingTemplate messagingTemplate) {
        this.roomRepository = roomRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // For sending and broadcasting the message in real-time
    @MessageMapping("/sendMessage/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, MessageRequest request) {
        Room room = roomRepository.findByRoomId(request.getRoomId());

        if (room == null) {
            throw new RuntimeException("Room not found");
        }

        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(request.getSender());
        message.setTimeStamp(LocalDateTime.now());

        room.getMessages().add(message);
        roomRepository.save(room);

        // Dynamically send message to the specific topic
        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }
}
