package com.fullStack.expenseTracker.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.requests.ResetPasswordRequestDto;
import com.fullStack.expenseTracker.exceptions.RoleNotFoundException;
import com.fullStack.expenseTracker.exceptions.UserNotFoundException;
import com.fullStack.expenseTracker.exceptions.UserServiceLogicException;
import com.fullStack.expenseTracker.services.AuthService;
import com.fullStack.expenseTracker.services.UserService;

import jakarta.validation.Valid;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5000"})
@RestController
@RequestMapping("/mywallet/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDto<?>> getAllUsers(@Param("pageNumber") int pageNumber,
                                                         @Param("pageSize") int pageSize,
                                                         @Param("searchKey") String searchKey)
            throws RoleNotFoundException, UserServiceLogicException {
        return userService.getAllUsers(pageNumber, pageSize, searchKey);
    }

    @DeleteMapping("/disable")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDto<?>> disableUser(@Param("userId") long userId)
            throws UserNotFoundException, UserServiceLogicException {
        return userService.enableOrDisableUser(userId);
    }

    @PutMapping("/enable")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDto<?>> enableUser(@Param("userId") long userId)
            throws UserNotFoundException, UserServiceLogicException {
        return userService.enableOrDisableUser(userId);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDto<?>> deleteUser(@Param("userId") long userId)
            throws UserNotFoundException, UserServiceLogicException {
        return userService.deleteUser(userId);
    }

    @PostMapping("/settings/changePassword")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDto<?>> changePassword(@RequestBody @Valid ResetPasswordRequestDto resetPasswordRequestDto)
            throws UserNotFoundException, UserServiceLogicException {
        return authService.resetPassword(resetPasswordRequestDto);
    }

    @PostMapping("/settings/profileImg")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDto<?>> uploadProfileImg(@RequestParam("email") String email, @RequestParam("file") @Valid MultipartFile file)
            throws UserNotFoundException, UserServiceLogicException {
        return userService.uploadProfileImg(email, file);
    }

    @GetMapping("/settings/profileImg")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDto<?>> getProfileImg(@RequestParam("email") String email)
            throws UserNotFoundException, UserServiceLogicException, IOException {
        return userService.getProfileImg(email);
    }

    @DeleteMapping("/settings/profileImg")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDto<?>> deleteProfileImg(@RequestParam("email") String email)
            throws UserNotFoundException, UserServiceLogicException, IOException {
        return userService.deleteProfileImg(email);
    }
}
