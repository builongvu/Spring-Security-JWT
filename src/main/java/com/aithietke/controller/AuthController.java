package com.aithietke.controller;

import com.aithietke.constant.RoleEnum;
import com.aithietke.dto.*;
import com.aithietke.entity.RefreshToken;
import com.aithietke.entity.Role;
import com.aithietke.entity.User;
import com.aithietke.exception.TokenRefreshException;
import com.aithietke.repository.RoleRepository;
import com.aithietke.repository.UserRepository;
import com.aithietke.security.JwtUtils;
import com.aithietke.security.UserDetailsImpl;
import com.aithietke.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping(value = "/signin")
    public ResponseEntity<?> login(@Valid @RequestBody SigninRequest signinRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signinRequest.getUsername(), signinRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(
                new JwtResponse(jwt,
                        refreshToken.getToken(),
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken !"));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use !"));

        }

        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .build();

        Set<String> asignRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        // Neu khong truyen thi set role mac dinh la ROLE_USER
        if (asignRoles == null) {
            Role userRole = roleRepository.findByName(RoleEnum.ROLE_USER.getRole())
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found !"));
            roles.add(userRole);
        } else {
            asignRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(RoleEnum.ROLE_ADMIN.getRole())
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found !"));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(RoleEnum.ROLE_MODERATOR.getRole())
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found !"));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(RoleEnum.ROLE_USER.getRole())
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found !"));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully !"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {
        String refreshToken = tokenRefreshRequest.getRefreshToken();

        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, refreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token is not in database !"));
    }

}
