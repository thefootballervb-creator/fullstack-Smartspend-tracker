package com.fullStack.expenseTracker.services.impls;

import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.requests.ResetPasswordRequestDto;
import com.fullStack.expenseTracker.dto.requests.SignUpRequestDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.exceptions.RoleNotFoundException;
import com.fullStack.expenseTracker.exceptions.UserNotFoundException;
import com.fullStack.expenseTracker.exceptions.UserServiceLogicException;
import com.fullStack.expenseTracker.factories.RoleFactory;
import com.fullStack.expenseTracker.models.Role;
import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.repository.UserRepository;
import com.fullStack.expenseTracker.services.NotificationService;
import com.fullStack.expenseTracker.services.UserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private RoleFactory roleFactory;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthServiceImpl authService;

    @BeforeEach
    public void setUp() throws RoleNotFoundException {
        // Return a dummy role for both user and admin
        given(roleFactory.getInstance(anyString())).willAnswer(inv -> new Role());
        given(passwordEncoder.encode(anyString())).willReturn("ENCODED");
    }

    @Test
    @SuppressWarnings("null") // ArgumentCaptor.capture() doesn't preserve @NonNull contract, but we verify non-null after capture
    void save_ShouldCreateUserAndSendVerification() throws Exception {
        SignUpRequestDto dto = new SignUpRequestDto("admin", "admin@test.com", "admin123", Set.of("admin"));

        given(userService.existsByUsername(dto.getUserName())).willReturn(false);
        given(userService.existsByEmail(dto.getEmail())).willReturn(false);

        ResponseEntity<?> response = authService.save(dto);

        // Persisted entity captured and verified non-null
        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(savedUserCaptor.capture());
        User savedUser = Objects.requireNonNull(savedUserCaptor.getValue());
        assertThat(savedUser).isNotNull();

        // Verification mail triggered with a non-null user
        ArgumentCaptor<User> mailedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(notificationService, times(1)).sendUserRegistrationVerificationEmail(mailedUserCaptor.capture());
        assertThat(mailedUserCaptor.getValue()).isNotNull();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Object responseBody = response.getBody();
        assertThat(responseBody).isInstanceOf(ApiResponseDto.class);
        ApiResponseDto<?> body = (ApiResponseDto<?>) responseBody;
        assertThat(body.getStatus()).isEqualTo(ApiResponseStatus.SUCCESS);
    }

    @Test
    @SuppressWarnings("null") // ArgumentCaptor.capture() doesn't preserve @NonNull contract, but we verify non-null after capture
    void resetPassword_ShouldEncodeAndSave() throws UserNotFoundException, UserServiceLogicException {
        ResetPasswordRequestDto dto = new ResetPasswordRequestDto();
        dto.setEmail("user@test.com");
        dto.setNewPassword("newPass123");
        dto.setCurrentPassword(""); // current password optional in flow

        User u = new User();
        u.setEmail(dto.getEmail());
        u.setPassword("OLD");

        given(userService.existsByEmail(dto.getEmail())).willReturn(true);
        given(userService.findByEmail(dto.getEmail())).willReturn(u);

        authService.resetPassword(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = Objects.requireNonNull(captor.getValue());
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPassword()).isEqualTo("ENCODED");
    }
}


