package com.fullStack.expenseTracker.services;

import java.util.Map;
import java.util.Objects;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationGateway {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public void sendAlert(String type, Map<String, Object> payload) {
        simpMessagingTemplate.convertAndSend("/topic/alerts", Objects.requireNonNull(Map.of(
                "type", type,
                "data", payload
        )));
    }
}


