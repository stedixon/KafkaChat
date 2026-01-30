package com.testapp.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageKey {

    @JsonProperty("chat_room_id")
    private String orgId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("message_id")
    private String messageId;
}
