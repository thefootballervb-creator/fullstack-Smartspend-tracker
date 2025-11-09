package com.fullStack.expenseTracker.controllers;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fullStack.expenseTracker.dto.requests.SignUpRequestDto;
import com.fullStack.expenseTracker.services.AuthService;

@ExtendWith(MockitoExtension.class)
class SignUpControllerTest {

    @Mock private AuthService authService;
    @InjectMocks private SignUpController controller;

    @Test
    void registerUser_ShouldReturnCreated() throws Exception {
        given(authService.save(any(SignUpRequestDto.class)))
                .willReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        ResponseEntity<?> res = controller.registerUser(new SignUpRequestDto("u","e@e.com","password123", Set.of("admin")));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}


