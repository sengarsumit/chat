package com.example.chatify.chat.DTO.config;

import com.example.chatify.chat.DTO.UserRegisterDTO;
import com.example.chatify.chat.model.users;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    @Autowired
    private ModelMapper modelMapper;

    public users dtoToUsers(UserRegisterDTO userRegisterDTO) {
        users user=modelMapper.map(userRegisterDTO,users.class);
        return user;
    }

}
