package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.ResponseEntity.status;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/auth")
public class AuthenticationController {

    //TODO Melhorar método registerUser() retirando os if's utilizando exceptions customizadas com o ExceptionHandler
    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Validated(UserDto.UserView.RegistrationPost.class)
                                              @JsonView(UserDto.UserView.RegistrationPost.class)
                                                      UserDto userDto){

        if(userService.existsByUsername(userDto.getUsername())){
            return status(HttpStatus.CONFLICT).body("Error: Username is already taken!");
        }

        if(userService.existsByEmail(userDto.getEmail())){
            return status(HttpStatus.CONFLICT).body("Error: Email is already taken!");
        }

        var userModel = new UserModel();
        BeanUtils.copyProperties(userDto, userModel);
        userModel.setUserActived();
        userModel.setUserAsStudent();
        //userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC"))); //adicionado anotação no UserModel
        //userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        this.userService.save(userModel);

        return status(HttpStatus.CREATED).body(userModel);
    }

}
