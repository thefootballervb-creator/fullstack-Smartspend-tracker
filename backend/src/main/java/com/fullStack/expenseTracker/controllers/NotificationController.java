package com.fullStack.expenseTracker.controllers;

import com.fullStack.expenseTracker.services.NotificationGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mywallet/notify")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5000"})
public class NotificationController {

    private final NotificationGateway gateway;

    // Simple trigger endpoint to test notifications quickly
    @PostMapping("/test")
    public ResponseEntity<Void> sendTest(@RequestBody Map<String, Object> body) {
        gateway.sendAlert("TEST", body);
        return ResponseEntity.accepted().build();
    }
}


