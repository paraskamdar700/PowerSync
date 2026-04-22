package com.example.BuildingManagement.power.Config;

import com.example.BuildingManagement.power.WebSocket.PowerWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PowerWebSocketHandler powerWebSocketHandler;

    @Autowired
    public WebSocketConfig(PowerWebSocketHandler powerWebSocketHandler) {
        this.powerWebSocketHandler = powerWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(powerWebSocketHandler, "/ws/telemetry")
                .setAllowedOrigins("*");
    }
}
