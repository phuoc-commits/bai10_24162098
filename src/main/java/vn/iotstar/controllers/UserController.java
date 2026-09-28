package vn.iotstar.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.User;
import vn.iotstar.models.UserResponse;
import vn.iotstar.services.UserService;

import java.util.List;

@RequestMapping("/users")
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(UserResponse.from(currentUser));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> allUsers() {
        List<User> users = userService.allUsers();
        return ResponseEntity.ok(users.stream().map(UserResponse::from).toList());
    }
}