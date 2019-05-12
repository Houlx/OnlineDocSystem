package com.hou.gradproj.docmanagesys.controller;

import com.hou.gradproj.docmanagesys.model.User;
import com.hou.gradproj.docmanagesys.payload.UserIdentityAvailability;
import com.hou.gradproj.docmanagesys.payload.UserSummary;
import com.hou.gradproj.docmanagesys.repository.UserRepository;
import com.hou.gradproj.docmanagesys.security.CurrentUser;
import com.hou.gradproj.docmanagesys.security.UserPrincipal;
import com.hou.gradproj.docmanagesys.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserRepository userRepository;

    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        logger.info(currentUser.toString());
        return new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName(), currentUser.getStorageRoom(), currentUser.getAlreadyUsedRoom());
    }

    @GetMapping("/checkUsernameAvailability")
    public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
        Boolean isAvailable = !userRepository.existsByUsername(username);
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/checkEmailAvailability")
    public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
        Boolean isAvailable = !userRepository.existsByEmail(email);
        return new UserIdentityAvailability(isAvailable);
    }

    @PostMapping("/me/{newName}")
    public UserSummary changeName(@CurrentUser UserPrincipal currentUser, @PathVariable String newName) {
        User user = userService.changeName(currentUser, newName);
        return new UserSummary(user.getId(), user.getUsername(), user.getName(), user.getStorageRoom(), user.getAlreadyUsedRoom());
    }

    @PostMapping("/me/password")
    public UserSummary changePassword(@CurrentUser UserPrincipal currentUser, @RequestParam("newPassword") String newPassword) {
        User user = userService.changePassword(currentUser, newPassword);
        return new UserSummary(user.getId(), user.getUsername(), user.getName(), user.getStorageRoom(), user.getAlreadyUsedRoom());
    }

}
