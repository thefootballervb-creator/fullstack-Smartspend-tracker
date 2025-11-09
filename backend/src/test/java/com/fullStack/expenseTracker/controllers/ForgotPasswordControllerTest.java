package com.fullStack.expenseTracker.controllers;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.requests.ResetPasswordRequestDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.services.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordControllerTest {

    @Mock private AuthService authService;
    @InjectMocks private ForgotPasswordController controller;

    @Test
    void verifyEmail_ShouldReturnAccepted() throws Exception {
        given(authService.verifyEmailAndSendForgotPasswordVerificationEmail(eq("a@b.com")))
                .willReturn(ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(new ApiResponseDto<>(ApiResponseStatus.SUCCESS, HttpStatus.ACCEPTED, "OK")));

        ResponseEntity<ApiResponseDto<?>> res = controller.verifyEmail("a@b.com");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void verifyCode_ShouldReturnOk() throws Exception {
        given(authService.verifyForgotPasswordVerification(eq("123456")))
                .willReturn(ResponseEntity.ok(new ApiResponseDto<>(ApiResponseStatus.SUCCESS, HttpStatus.OK, "OK")));
        ResponseEntity<ApiResponseDto<?>> res = controller.verifyCode("123456");
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void resetPassword_ShouldReturnCreated() throws Exception {
        given(authService.resetPassword(any(ResetPasswordRequestDto.class)))
                .willReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponseDto<>(ApiResponseStatus.SUCCESS, HttpStatus.CREATED, "OK")));
        ResponseEntity<ApiResponseDto<?>> res = controller.resetPassword(new ResetPasswordRequestDto());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void resendEmail_ShouldReturnAccepted() throws Exception {
        given(authService.verifyEmailAndSendForgotPasswordVerificationEmail(eq("a@b.com")))
                .willReturn(ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(new ApiResponseDto<>(ApiResponseStatus.SUCCESS, HttpStatus.ACCEPTED, "OK")));
        ResponseEntity<ApiResponseDto<?>> res = controller.resendEmail("a@b.com");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }
}


