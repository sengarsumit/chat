package com.example.chatify.chat.DTO.config;

import com.example.chatify.chat.DTO.UserRegisterDTO;
import com.example.chatify.chat.DTO.UserResponseDTO;
import com.example.chatify.chat.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    @Autowired
    private ModelMapper modelMapper;

    public User dtoToUsers(UserRegisterDTO userRegisterDTO) {
        User user=modelMapper.map(userRegisterDTO, User.class);
        return user;
    }
    public UserResponseDTO userToDto(User user)
    {
        UserResponseDTO userResponseDTO=modelMapper.map(user,UserResponseDTO.class);
        return userResponseDTO;
    }

}
