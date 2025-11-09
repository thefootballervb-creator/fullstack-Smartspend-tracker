package com.fullStack.expenseTracker.controllers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.fullStack.expenseTracker.dto.requests.SignInRequestDto;
import com.fullStack.expenseTracker.security.UserDetailsImpl;
import com.fullStack.expenseTracker.security.jwt.JwtUtils;

@ExtendWith(MockitoExtension.class)
class SignInControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks private SignInController controller;

    @Test
    void signIn_ShouldReturnOkWithJwt() {
        SignInRequestDto dto = new SignInRequestDto("admin@test.com", "pass");

        UserDetailsImpl principal = new UserDetailsImpl(1L, "admin", dto.getEmail(), "ENC", List.of(() -> "ROLE_ADMIN"), true);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(auth);
        given(jwtUtils.generateJwtToken(any(Authentication.class))).willReturn("token");

        ResponseEntity<?> res = controller.signIn(dto);

        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
    }
}


