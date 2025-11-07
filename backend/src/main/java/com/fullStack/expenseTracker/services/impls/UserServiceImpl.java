package com.fullStack.expenseTracker.services.impls;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.reponses.PageResponseDto;
import com.fullStack.expenseTracker.dto.reponses.UserResponseDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.enums.ETransactionType;
import com.fullStack.expenseTracker.exceptions.RoleNotFoundException;
import com.fullStack.expenseTracker.exceptions.UserNotFoundException;
import com.fullStack.expenseTracker.exceptions.UserServiceLogicException;
import com.fullStack.expenseTracker.factories.RoleFactory;
import com.fullStack.expenseTracker.models.Transaction;
import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.repository.TransactionRepository;
import com.fullStack.expenseTracker.repository.TransactionTypeRepository;
import com.fullStack.expenseTracker.repository.UserRepository;
import com.fullStack.expenseTracker.services.UserService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    RoleFactory roleFactory;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @Autowired
    private com.fullStack.expenseTracker.repository.SavedTransactionRepository savedTransactionRepository;

    @Autowired
    private com.fullStack.expenseTracker.repository.BudgetRepository budgetRepository;

    @Value("${app.user.profile.upload.dir}")
    private String userProfileUploadDir;


    @Override
    public ResponseEntity<ApiResponseDto<?>> getAllUsers(int pageNumber, int pageSize, String searchKey)
            throws RoleNotFoundException, UserServiceLogicException {

        Pageable pageable =  PageRequest.of(pageNumber, pageSize);

        Page<User> users = userRepository.findAll(pageable, roleFactory.getInstance("user").getId(), searchKey);

        try {
            List<UserResponseDto> userResponseDtoList = new ArrayList<>();

            for (User u: users) {
                userResponseDtoList.add(userToUserResponseDto(u));
            }

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponseDto<>(
                            ApiResponseStatus.SUCCESS,
                            HttpStatus.OK,
                            new PageResponseDto<>(userResponseDtoList, users.getTotalPages(), users.getTotalElements())
                    )
            );
        } catch (org.springframework.dao.DataAccessException | IllegalArgumentException | NullPointerException e) {
            log.error("Failed to fetch All users: " + e.getMessage());
            throw new UserServiceLogicException("Failed to fetch All users: Try again later!");
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> enableOrDisableUser(long userId)
            throws UserNotFoundException, UserServiceLogicException {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User not found with id " + userId)
        );

        try {

            user.setEnabled(!user.isEnabled());
            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponseDto<>(
                            ApiResponseStatus.SUCCESS, HttpStatus.OK, "User has been updated successfully!"
                    )
            );
        }catch(Exception e) {
            log.error("Failed to enable/disable user: " + e.getMessage());
            throw new UserServiceLogicException("Failed to update user: Try again later!");
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> deleteUser(long userId)
            throws UserNotFoundException, UserServiceLogicException {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User not found with id " + userId)
        );

        try {
            log.info("Starting deletion process for user ID: {}", userId);
            
            // Delete all transactions for this user
            List<Transaction> userTransactions = transactionRepository.findByUserId(userId);
            if (!userTransactions.isEmpty()) {
                transactionRepository.deleteAll(userTransactions);
                transactionRepository.flush(); // Ensure transactions are deleted before proceeding
                log.info("Deleted {} transactions for user {}", userTransactions.size(), userId);
            } else {
                log.info("No transactions found for user {}", userId);
            }

            // Delete all saved transactions for this user
            List<com.fullStack.expenseTracker.models.SavedTransaction> savedTransactions = 
                    savedTransactionRepository.findByUserIdOrderByUpcomingDateAsc(userId);
            if (!savedTransactions.isEmpty()) {
                savedTransactionRepository.deleteAll(savedTransactions);
                savedTransactionRepository.flush(); // Ensure saved transactions are deleted
                log.info("Deleted {} saved transactions for user {}", savedTransactions.size(), userId);
            } else {
                log.info("No saved transactions found for user {}", userId);
            }

            // Delete all budgets for this user
            List<com.fullStack.expenseTracker.models.Budget> budgets = budgetRepository.findByUserId(userId);
            if (!budgets.isEmpty()) {
                budgetRepository.deleteAll(budgets);
                budgetRepository.flush(); // Ensure budgets are deleted
                log.info("Deleted {} budgets for user {}", budgets.size(), userId);
            } else {
                log.info("No budgets found for user {}", userId);
            }

            // Delete profile image if exists
            if (user.getProfileImgUrl() != null && !user.getProfileImgUrl().isEmpty()) {
                try {
                    File profileImgFile = new File(user.getProfileImgUrl());
                    if (profileImgFile.exists()) {
                        if (profileImgFile.delete()) {
                            log.info("Deleted profile image for user {}", userId);
                        } else {
                            log.warn("Failed to delete profile image file for user {}", userId);
                        }
                    }
                } catch (SecurityException e) {
                    log.warn("Security exception while deleting profile image for user {}: {}", userId, e.getMessage());
                }
            }

            // Clear user roles relationship (ManyToMany)
            user.getRoles().clear();
            userRepository.saveAndFlush(user);
            log.info("Cleared user roles for user {}", userId);

            // Delete the user
            userRepository.delete(user);
            userRepository.flush(); // Ensure user is deleted
            
            // Verify deletion
            if (userRepository.existsById(userId)) {
                log.error("User {} still exists after deletion attempt!", userId);
                throw new UserServiceLogicException("Failed to delete user: User still exists in database!");
            }
            
            log.info("Successfully deleted user {} - verified user no longer exists", userId);

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponseDto<>(
                            ApiResponseStatus.SUCCESS, HttpStatus.OK, "User has been deleted successfully!"
                    )
            );
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Data integrity violation while deleting user {}: {}", userId, e.getMessage(), e);
            throw new UserServiceLogicException("Failed to delete user: Database constraint violation. Please try again later!");
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Data access exception while deleting user {}: {}", userId, e.getMessage(), e);
            throw new UserServiceLogicException("Failed to delete user: Database error. Please try again later!");
        } catch (Exception e) {
            log.error("Unexpected error while deleting user {}: {}", userId, e.getMessage(), e);
            e.printStackTrace();
            throw new UserServiceLogicException("Failed to delete user: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> uploadProfileImg(String email, MultipartFile file)
            throws UserServiceLogicException, UserNotFoundException {
        if (existsByEmail(email)) {
            try {
                User user = findByEmail(email);
                String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "File name cannot be null");
                String extention = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFileName = user.getUsername().concat(extention);
                Path targetLocation = Paths.get(userProfileUploadDir).resolve(newFileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                user.setProfileImgUrl(String.valueOf(targetLocation));
                userRepository.save(user);
                return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDto<>(
                        ApiResponseStatus.SUCCESS,
                        HttpStatus.CREATED,
                        "Profile image successfully updated!"
                ));
            } catch (java.io.IOException | org.springframework.dao.DataAccessException | IllegalArgumentException | NullPointerException e) {
                log.error("Failed to update profile img: {}", e.getMessage());
                throw new UserServiceLogicException("Failed to update profile image: Try again later!");
            }
        }

        throw new UserNotFoundException("User not found with email " + email);
    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> getProfileImg(String email) throws UserNotFoundException, IOException, UserServiceLogicException {
        if (existsByEmail(email)) {
            try{
                User user = findByEmail(email);

                if (user.getProfileImgUrl() != null) {
                    Path profileImgPath = Paths.get(user.getProfileImgUrl());

                    byte[] imageBytes = Files.readAllBytes(profileImgPath);
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto<>(
                            ApiResponseStatus.SUCCESS,
                            HttpStatus.OK,
                            base64Image
                    ));
                }else {
                    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto<>(
                            ApiResponseStatus.SUCCESS,
                            HttpStatus.OK,
                            null
                    ));
                }

            } catch (java.io.IOException | org.springframework.dao.DataAccessException | IllegalArgumentException | NullPointerException e) {
                log.error("Failed to get profile img: {}", e.getMessage());
                throw new UserServiceLogicException("Failed to get profile image: Try again later!");
            }
        }

        throw new UserNotFoundException("User not found with email " + email);
    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> deleteProfileImg(String email) throws UserServiceLogicException, UserNotFoundException {
        if (existsByEmail(email)) {
            try{
                User user = findByEmail(email);

                File file = new File(user.getProfileImgUrl());
                if (file.exists()) {
                    if (file.delete()) {
                        user.setProfileImgUrl(null);
                        userRepository.save(user);
                        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto<>(
                                ApiResponseStatus.SUCCESS,
                                HttpStatus.OK,
                                "Profile image removed successfully!"
                        ));
                    }else {
                        throw new UserServiceLogicException("Failed to remove profile image: Try again later!");
                    }
                }
            } catch (org.springframework.dao.DataAccessException | IllegalArgumentException | NullPointerException | SecurityException e) {
                log.error("Failed to get profile img: {}", e.getMessage());
                throw new UserServiceLogicException("Failed to remove profile image: Try again later!");
            }
        }

        throw new UserNotFoundException("User not found with email " + email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User findByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " +  email));
    }

    private UserResponseDto userToUserResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                transactionRepository.findTotalByUserAndTransactionType(
                        user.getId(),
                        transactionTypeRepository.findByTransactionTypeName(ETransactionType.TYPE_EXPENSE).getTransactionTypeId(),
                        LocalDate.now().getMonthValue(),
                        LocalDate.now().getYear()
                ),
                transactionRepository.findTotalByUserAndTransactionType(
                        user.getId(),
                        transactionTypeRepository.findByTransactionTypeName(ETransactionType.TYPE_INCOME).getTransactionTypeId(),
                        LocalDate.now().getMonthValue(),
                        LocalDate.now().getYear()
                ),
                transactionRepository.findTotalNoOfTransactionsByUser(user.getId(), LocalDate.now().getMonthValue(),
                        LocalDate.now().getYear())
        );
    }

}
