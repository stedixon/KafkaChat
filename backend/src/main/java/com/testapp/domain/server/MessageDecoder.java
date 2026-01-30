package com.testapp.domain.server;

import com.google.gson.Gson;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;

public class MessageDecoder implements Decoder.Text<ChatMessage> {

    private static Gson gson = new Gson();

    @Override
    public ChatMessage decode(String s) throws DecodeException {
        return gson.fromJson(s, ChatMessage.class);
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null);
    }

    @Override
    public void destroy() {
        // Close resources
    }
}
