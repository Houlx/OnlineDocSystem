package com.hou.gradproj.docmanagesys.controller;

import com.hou.gradproj.docmanagesys.exception.ResourceNotFoundException;
import com.hou.gradproj.docmanagesys.model.User;
import com.hou.gradproj.docmanagesys.payload.ApiResponse;
import com.hou.gradproj.docmanagesys.payload.PagedResponse;
import com.hou.gradproj.docmanagesys.payload.UserProfile;
import com.hou.gradproj.docmanagesys.repository.UserRepository;
import com.hou.gradproj.docmanagesys.service.AdminService;
import com.hou.gradproj.docmanagesys.util.AppConstants;
import com.hou.gradproj.docmanagesys.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final UserRepository userRepository;

    private final AdminService adminService;

    @Autowired
    public AdminController(UserRepository userRepository, AdminService adminService) {
        this.userRepository = userRepository;
        this.adminService = adminService;
    }

    @GetMapping
    public PagedResponse<UserProfile> getAllUsers(@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                  @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Page<User> allUsers = adminService.getUsers(page, size);

        List<UserProfile> userProfiles;
        if (allUsers.getNumberOfElements() == 0) {
            userProfiles = Collections.emptyList();
        } else {
            userProfiles = allUsers.map(ModelMapper::mapUserToUserProfile).getContent();
        }
        return new PagedResponse<>(userProfiles, allUsers.getNumber(), allUsers.getSize(), allUsers.getTotalElements(), allUsers.getTotalPages(), allUsers.isLast());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (adminService.deleteUser(id)) {
            return ResponseEntity.ok(new ApiResponse(true, "User deleted"));
        } else {
            return new ResponseEntity<>(new ApiResponse(false, "Delete user failed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> modifyStorageRoom(@PathVariable Long id, @RequestParam(value = "room") int gigaByte) {
        long bytes = gigaByte * 1024 * 1024 * 1024;
        if (adminService.modifyStorageRoom(id, BigInteger.valueOf(bytes))) {
            return ResponseEntity.ok(new ApiResponse(true, "Modified successfully"));
        } else {
            return new ResponseEntity<>(new ApiResponse(false, "Modify failed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{username}")
    public UserProfile getUserProfile(@PathVariable(value = "username") String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return ModelMapper.mapUserToUserProfile(user);
    }
}
