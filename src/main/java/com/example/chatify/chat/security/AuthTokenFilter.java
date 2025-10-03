package com.example.chatify.chat.security;

import com.example.chatify.chat.model.User;
import com.example.chatify.chat.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter  extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            String jwt=parseJwtCookie(request);
            if(jwt!=null && jwtUtil.validateToken(jwt))
            {
                String username=jwtUtil.getUsernameFromToken(jwt);
                User user=userRepository.findByUsername(username);
                if(user==null)
                {
                    throw new UsernameNotFoundException("user not found");
                }
                UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(user,null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
        }
        catch(Exception e)
        {
            logger.warn("cannot set user authentication",e.getMessage());
        }
        filterChain.doFilter(request,response);
    }
    private String parseJwtCookie(HttpServletRequest request) {
        if(request.getCookies()!=null)
        {
            for(Cookie cookie : request.getCookies())
            {
                if("accessToken".equals(cookie.getName()))
                {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
