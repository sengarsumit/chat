package com.example.chatify.chat.controller;

import com.example.chatify.chat.DTO.UserLoginDTO;
import com.example.chatify.chat.DTO.UserRegisterDTO;
import com.example.chatify.chat.DTO.config.UserMapper;
import com.example.chatify.chat.model.users;
import com.example.chatify.chat.repository.UserRepository;
import com.example.chatify.chat.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegisterDTO userRegisterDTO)
    {
        if(userRepository.existsByUsername(userRegisterDTO.getUsername()))
        {
            return new ResponseEntity<>("Username is already in use", HttpStatus.CONFLICT);
        }
        else if(userRepository.existsByEmail(userRegisterDTO.getEmail()))
        {
            return new ResponseEntity<>("Email is already in use", HttpStatus.CONFLICT);
        }
        users newUser=userMapper.dtoToUsers(userRegisterDTO);
        newUser.setPassword(encoder.encode(userRegisterDTO.getPassword()));
        userRepository.save(newUser);
        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletResponse httpServletResponse)
    {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDTO.getUsername(),userLoginDTO.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        users dbuser=userRepository.findByUsername(userDetails.getUsername());
        if(dbuser==null)
        {
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
        String accessToken= jwtUtil.generateAccessToken(dbuser.getUsername());
        String refreshToken= jwtUtil.generateRefreshToken(dbuser.getUsername());

        ResponseCookie accessCookie=ResponseCookie.from("accessToken",accessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(15*60)
                .build();

        ResponseCookie refreshCookie=ResponseCookie.from("refreshToken",refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(7*24*60*60)
                .build();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new ResponseEntity<>("Login successfull", HttpStatus.CREATED);

    }
}
