package com.cryptoauth.infosecurity.user.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.cryptoauth.infosecurity.user.dto.UserCreate;
import com.cryptoauth.infosecurity.user.dto.UserGet;
import com.cryptoauth.infosecurity.user.dto.UserResponseProfile;
import com.cryptoauth.infosecurity.user.dto.UserUpdate;
import com.cryptoauth.infosecurity.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public Page<UserGet> getAllUsers(@RequestParam(value = "search", required = false) String search, Pageable pageable) {
        return userService.getAllUsers(search, pageable);
    }

    @PostMapping("/register")
    public UserGet registerUser(@RequestBody UserCreate userCreate){
        return userService.createUser(userCreate);
    }

    @GetMapping("/users/{email}")
    public UserGet findUser(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @GetMapping("/users/user/{email}/id")
    public UserGet getIdUser(@PathVariable UUID id) {
        return userService.getUser(id);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserGet> updateUser(@PathVariable("id") UUID id, @RequestBody UserUpdate userUpdate) {
        return ResponseEntity.ok(userService.updateUser(id, userUpdate));
    }

    @DeleteMapping("/users/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserResponseProfile getMe(Authentication authentication) {
        // Вытаскивается из токена
        String username = authentication.getName();
        return userService.getMe(username);
    }
}
