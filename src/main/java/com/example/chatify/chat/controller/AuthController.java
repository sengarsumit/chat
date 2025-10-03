package com.example.chatify.chat.controller;

import com.example.chatify.chat.DTO.UserLoginDTO;
import com.example.chatify.chat.DTO.UserRegisterDTO;
import com.example.chatify.chat.DTO.UserResponseDTO;
import com.example.chatify.chat.DTO.config.UserMapper;
import com.example.chatify.chat.model.users;
import com.example.chatify.chat.repository.UserRepository;
import com.example.chatify.chat.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private ModelMapper modelMapper;

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

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@CookieValue(name = "accessToken",required = false) String accessToken)
    {
        if(accessToken==null)
        {
            return  new ResponseEntity<>("access token is null", HttpStatus.UNAUTHORIZED);
        }
        if(jwtUtil.validateToken(accessToken))
        {
            String username=jwtUtil.getUsernameFromToken(accessToken);
            users dbuser=userRepository.findByUsername(username);

            if(dbuser==null)
            {
                return new ResponseEntity<>("User not found associated with token or expired token",HttpStatus.NOT_FOUND);
            }
            UserResponseDTO userResponseDTO=userMapper.userToDto(dbuser);
            return new ResponseEntity<>(userResponseDTO,HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>("Invalid access token",HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request,HttpServletResponse response)
    {
        String refreshToken=null;

        if(request.getCookies()!=null)
        {
            for(Cookie cookie:request.getCookies())
            {
                if("refreshToken".equals(cookie.getName()))
                {
                    refreshToken=cookie.getValue();
                }
            }
        }
        if(refreshToken!=null && jwtUtil.validateToken(refreshToken))
        {
            String username=jwtUtil.getUsernameFromToken(refreshToken);
            users dbuser=userRepository.findByUsername(username);

            if(dbuser==null)
            {
                throw new UsernameNotFoundException("user not found");
            }
            String newAccessToken=jwtUtil.generateAccessToken(dbuser.getUsername());
            String newRefreshToken=jwtUtil.generateRefreshToken(dbuser.getUsername());

            ResponseCookie newAccessCookie=ResponseCookie.from("accessToken",newAccessToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(15*60)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, newAccessCookie.toString());
            return ResponseEntity.ok("access token refreshed successfully");
        }
        return  new ResponseEntity<>("Invalid refresh token",HttpStatus.UNAUTHORIZED);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response)
    {
        ResponseCookie deleteAccess=ResponseCookie.from("accessToken","")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie deleteRefresh=ResponseCookie.from("refreshToken","")
                .httpOnly(true)
                .secure(true)
                .sameSite("LAx")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());
        return ResponseEntity.ok("Logged out successfully");
    }
}
