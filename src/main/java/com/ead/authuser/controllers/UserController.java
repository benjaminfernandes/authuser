package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specifications.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.status;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge =  3600)//pode ser colocado globalmente atraves de config. Tem no ESR Algaworks
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(SpecificationTemplate.UserSpec spec,
                                                    @PageableDefault(page = 0, size = 10, sort = "userId",
                                                    direction = Sort.Direction.ASC) Pageable pageable){
        Page<UserModel> userModelPage = userService.findAll(spec, pageable);
        if(!userModelPage.isEmpty()){
            userModelPage.toList().forEach(user -> user.add(linkTo(methodOn(UserController.class)
                    .getOneUser(user.getUserId())).withSelfRel()));
        }
        return status(OK).body(userModelPage);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getOneUser(@PathVariable(value = "userId") UUID userId){
        Optional<UserModel> userModelOptional = existsUser(userId);
        if(!userModelOptional.isPresent()){
            return status(NOT_FOUND).body("User not found");
        } else {
            return status(OK).body(userModelOptional.get());
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable(value = "userId") UUID userId){
        Optional<UserModel> userModelOptional = existsUser(userId);
        if(!userModelOptional.isPresent()) {
            return status(NOT_FOUND).body("User not found");
        } else {
            this.userService.delete(userModelOptional.get());
            return status(OK).body("User deleted successful");
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable(value = "userId") UUID userId,
                                       @RequestBody @Validated(UserDto.UserView.UserPut.class)
                                       @JsonView(UserDto.UserView.UserPut.class) UserDto userDto){

        Optional<UserModel> userModelOptional = existsUser(userId);
        if(!userModelOptional.isPresent()) {
            return status(NOT_FOUND).body("User not found");
        } else {
            var userModel = userModelOptional.get();
            userModel.setFullName(userDto.getFullName());
            userModel.setPhoneNumber(userDto.getPhoneNumber());
            userModel.setCpf(userDto.getCpf());

            this.userService.save(userModel);
            return status(OK).body(userModel);
        }
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<?> updatePassword(@PathVariable(value = "userId") UUID userId,
                                       @RequestBody @Validated(UserDto.UserView.PasswordPut.class)
                                       @JsonView(UserDto.UserView.PasswordPut.class) UserDto userDto){

        Optional<UserModel> userModelOptional = existsUser(userId);
        if(!userModelOptional.isPresent()) {
            return status(NOT_FOUND).body("User not found");
        } if(!userModelOptional.get().getPassword().equals(userDto.getOldPassword())){
            return status(HttpStatus.CONFLICT).body("Error: Mismatched old password");
        } else {
            var userModel = userModelOptional.get();
            userModel.setPassword(userDto.getPassword());

            this.userService.save(userModel);
            return status(OK).body("Password updated successfully");
        }
    }

    @PutMapping("/{userId}/image")
    public ResponseEntity<?> updateImage(@PathVariable(value = "userId") UUID userId,
                                         @RequestBody @Validated(UserDto.UserView.ImagePut.class)
                                         @JsonView(UserDto.UserView.ImagePut.class) UserDto userDto){

        Optional<UserModel> userModelOptional = existsUser(userId);
        if(!userModelOptional.isPresent()) {
            return status(NOT_FOUND).body("User not found");
        } else {
            var userModel = userModelOptional.get();
            userModel.setImageUrl(userDto.getImageUrl());

            this.userService.save(userModel);
            return status(OK).body(userModel);
        }
    }

    private Optional<UserModel> existsUser(UUID userId){
        return this.userService.findById(userId);
    }
}
