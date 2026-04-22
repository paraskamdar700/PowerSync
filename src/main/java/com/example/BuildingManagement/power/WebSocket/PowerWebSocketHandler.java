package com.example.BuildingManagement.power.WebSocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class PowerWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("[WebSocket] Client connected: " + session.getId());
        System.out.println("[WebSocket] Active sessions: " + sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("[WebSocket] Client disconnected: " + session.getId());
        System.out.println("[WebSocket] Active sessions: " + sessions.size());
    }

    /**
     * Broadcasts a message to all connected WebSocket clients.
     * Automatically removes sessions that fail to send (stale/broken connections).
     *
     * @param message the JSON payload to broadcast
     */
    public void broadcast(String message) {
        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    System.err.println("[WebSocket] Failed to send to session " + session.getId() + ": " + e.getMessage());
                    sessions.remove(session);
                }
            } else {
                sessions.remove(session);
            }
        }
    }
}
