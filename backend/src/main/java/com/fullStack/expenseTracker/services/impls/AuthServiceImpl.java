package com.fullStack.expenseTracker.services.impls;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.reponses.JwtResponseDto;
import com.fullStack.expenseTracker.dto.requests.ResetPasswordRequestDto;
import com.fullStack.expenseTracker.dto.requests.SignUpRequestDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.exceptions.RoleNotFoundException;
import com.fullStack.expenseTracker.exceptions.UserAlreadyExistsException;
import com.fullStack.expenseTracker.exceptions.UserNotFoundException;
import com.fullStack.expenseTracker.exceptions.UserServiceLogicException;
import com.fullStack.expenseTracker.exceptions.UserVerificationFailedException;
import com.fullStack.expenseTracker.factories.RoleFactory;
import com.fullStack.expenseTracker.models.Role;
import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.repository.UserRepository;
import com.fullStack.expenseTracker.security.UserDetailsImpl;
import com.fullStack.expenseTracker.security.jwt.JwtUtils;
import com.fullStack.expenseTracker.services.AuthService;
import com.fullStack.expenseTracker.services.NotificationService;
import com.fullStack.expenseTracker.services.UserService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@Component
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    RoleFactory roleFactory;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${app.verificationCodeExpirationMs}")
    private long EXPIRY_PERIOD;

    @Override
    public ResponseEntity<?> save(SignUpRequestDto signUpRequestDto)
            throws UserAlreadyExistsException, UserServiceLogicException {
        if (userService.existsByUsername(signUpRequestDto.getUserName())) {
            throw new UserAlreadyExistsException("Registration Failed: username is already taken!");
        }
        if (userService.existsByEmail(signUpRequestDto.getEmail())) {
            throw new UserAlreadyExistsException("Registration Failed: email is already taken!");
        }

        try {
            log.info("Starting user registration for username: {}, email: {}", signUpRequestDto.getUserName(), signUpRequestDto.getEmail());
            
            // Create user with enabled=true and no verification code
            User user = createUserWithoutVerification(signUpRequestDto);
            log.info("User object created successfully");

            userRepository.save(user);
            log.info("User saved to database with ID: {} and enabled status: {}", user.getId(), user.isEnabled());
            
            // Create UserDetailsImpl for JWT token generation
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            
            // Create authentication object
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            
            // Generate JWT token
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            // Get user roles
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            log.info("Registration successful for user: {}. JWT token generated.", user.getEmail());

            // Return JWT response (similar to signin endpoint)
            return ResponseEntity.ok(JwtResponseDto.builder()
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .id(userDetails.getId())
                    .token(jwt)
                    .roles(roles)
                    .build());

        } catch (RoleNotFoundException e) {
            log.error("Registration failed - Role not found: {}", e.getMessage(), e);
            throw new UserServiceLogicException("Registration failed: System configuration error. Please contact support.");
        } catch (Exception e) {
            log.error("Registration failed - Unexpected error: {} - Type: {}", e.getMessage(), e.getClass().getName(), e);
            e.printStackTrace();
            throw new UserServiceLogicException("Registration failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

    }

    @Override
    public ResponseEntity<?> verifyRegistrationVerification(String code) throws UserVerificationFailedException {
        User user = userRepository.findByVerificationCode(code);

        if (user == null || user.isEnabled()) {
            throw new UserVerificationFailedException("Verification failed: invalid verification code!");
        }

        long currentTimeInMs = System.currentTimeMillis();
        long codeExpiryTimeInMillis = user.getVerificationCodeExpiryTime().getTime();

        if (currentTimeInMs > codeExpiryTimeInMillis) {
            throw new UserVerificationFailedException("Verification failed: expired verification code!");
        }

        user.setVerificationCode(null);
        user.setVerificationCodeExpiryTime(null);
        user.setEnabled(true);
        userRepository.save(user);

        // Create UserDetailsImpl for JWT token generation
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        
        // Create authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        
        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // Get user roles
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Return JWT response (similar to signin endpoint)
        return ResponseEntity.ok(JwtResponseDto.builder()
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .id(userDetails.getId())
                .token(jwt)
                .roles(roles)
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> resendVerificationCode(String email) throws UserNotFoundException, UserServiceLogicException {

        User user = userService.findByEmail(email);

        try {
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiryTime(calculateCodeExpirationTime());
            user.setEnabled(false);

            userRepository.save(user);
            notificationService.sendUserRegistrationVerificationEmail(user);

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto<>(
                    ApiResponseStatus.SUCCESS, HttpStatus.OK, "Verification email has been resent successfully!")
            );
        } catch (jakarta.mail.MessagingException | java.io.UnsupportedEncodingException | RuntimeException e) {
            log.error("Registration verification failed: {}", e.getMessage());
            throw new UserServiceLogicException("Registration failed: Something went wrong!");
        }

    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> verifyEmailAndSendForgotPasswordVerificationEmail(String email) throws UserServiceLogicException, UserNotFoundException {
        if (userService.existsByEmail(email)) {
            try {
                User user = userService.findByEmail(email);
                user.setVerificationCode(generateVerificationCode());
                user.setVerificationCodeExpiryTime(calculateCodeExpirationTime());
                userRepository.save(user);

                notificationService.sendForgotPasswordVerificationEmail(user);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponseDto<>(
                        ApiResponseStatus.SUCCESS,
                        HttpStatus.ACCEPTED,
                        "Verification successful: Email sent successfully!"
                ));
            } catch (jakarta.mail.MessagingException | java.io.UnsupportedEncodingException | org.springframework.dao.DataAccessException | IllegalArgumentException e) {
                log.error("Reset password email verification failed: {}", e.getMessage());
                throw new UserServiceLogicException("Verification failed: Something went wrong!");
            }
        }

        throw new UserNotFoundException("Verification failed: User not found with email " + email + "!");

    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> verifyForgotPasswordVerification(String code) throws UserVerificationFailedException, UserServiceLogicException {
        User user = userRepository.findByVerificationCode(code);

        if (user == null) {
            throw new UserVerificationFailedException("Verification failed: invalid verification code!");
        }

        long currentTimeInMs = System.currentTimeMillis();
        long codeExpiryTimeInMillis = user.getVerificationCodeExpiryTime().getTime();

        if (currentTimeInMs > codeExpiryTimeInMillis) {
            throw new UserVerificationFailedException("Verification failed: expired verification code!");
        }

        try {

            user.setVerificationCode(null);
            user.setVerificationCodeExpiryTime(null);
            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponseDto<>(
                    ApiResponseStatus.SUCCESS, HttpStatus.ACCEPTED, "Verification successful: User account has been verified!"
            ));
        } catch (org.springframework.dao.DataAccessException | IllegalArgumentException | NullPointerException e) {
            log.error("Reset password verification failed: {}", e.getMessage());
            throw new UserServiceLogicException("Verification failed: Something went wrong!" + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> resetPassword(ResetPasswordRequestDto resetPasswordDto) throws UserNotFoundException, UserServiceLogicException {
        if (userService.existsByEmail(resetPasswordDto.getEmail())) {
            try {
                User user = userService.findByEmail(resetPasswordDto.getEmail());

                if (!resetPasswordDto.getCurrentPassword().isEmpty()) {
                    if (!passwordEncoder.matches(resetPasswordDto.getCurrentPassword(), user.getPassword())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(
                                ApiResponseStatus.FAILED,
                                HttpStatus.BAD_REQUEST,
                                "Reset password not successful: current password is incorrect!!"
                        ));
                    }
                }
                user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));

                userRepository.save(user);

                return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDto<>(
                        ApiResponseStatus.SUCCESS,
                        HttpStatus.CREATED,
                        "Reset successful: Password has been successfully reset!"
                ));
            } catch (org.springframework.dao.DataAccessException | IllegalArgumentException | NullPointerException e) {
                log.error("Resetting password failed: {}", e.getMessage());
                throw new UserServiceLogicException("Failed to reset your password: Try again later!");
            }
        }

        throw new UserNotFoundException("User not found with email " + resetPasswordDto.getEmail());
    }

    @NonNull
    private User createUser(SignUpRequestDto signUpRequestDto) throws RoleNotFoundException {
        return new User(
                signUpRequestDto.getUserName(),
                signUpRequestDto.getEmail(),
                passwordEncoder.encode(signUpRequestDto.getPassword()),
                generateVerificationCode(),
                calculateCodeExpirationTime(),
                false,
                determineRoles(signUpRequestDto.getRoles())
        );
    }

    @NonNull
    private User createUserWithoutVerification(SignUpRequestDto signUpRequestDto) throws RoleNotFoundException {
        return new User(
                signUpRequestDto.getUserName(),
                signUpRequestDto.getEmail(),
                passwordEncoder.encode(signUpRequestDto.getPassword()),
                null,  // No verification code
                null,  // No verification code expiry
                true,  // User enabled immediately
                determineRoles(signUpRequestDto.getRoles())
        );
    }

    private String generateVerificationCode() {
        return String.valueOf((int) (Math.random() * 1000000));
    }

    private Date calculateCodeExpirationTime() {
        long currentTimeInMs = System.currentTimeMillis();
        return new Date(currentTimeInMs + EXPIRY_PERIOD);
    }

    private Set<Role> determineRoles(Set<String> strRoles) throws RoleNotFoundException {
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            roles.add(roleFactory.getInstance("user"));
        } else {
            for (String role : strRoles) {
                roles.add(roleFactory.getInstance(role));
            }
        }
        return roles;
    }



}