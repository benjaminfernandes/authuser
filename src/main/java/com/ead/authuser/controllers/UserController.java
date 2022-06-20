package com.ead.authuser.controllers;

import com.ead.authuser.configs.security.AuthenticationCurrentUserService;
import com.ead.authuser.configs.security.UserDetailsImpl;
import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specifications.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge =  3600)//pode ser colocado globalmente atraves de config. Tem no ESR Algaworks
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationCurrentUserService authenticationCurrentUserService;

   // @PreAuthorize("hasAnyRole('ROLE_USER')") para testar a hierarquia de acesso configurada no websecurityconfig
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(SpecificationTemplate.UserSpec spec,
                                                       @PageableDefault(page = 0, size = 10, sort = "userId",
                                                    direction = Sort.Direction.ASC) Pageable pageable,
                                                       Authentication authentication){

        //Demonstra uma forma de obter o user atual com parametro
        UserDetails userDetails = (UserDetailsImpl) authentication.getPrincipal();
        log.info("Authentication: {}", userDetails.getUsername());

        Page<UserModel> userModelPage = userService.findAll(spec, pageable);
        if(!userModelPage.isEmpty()){
            userModelPage.toList().forEach(user -> user.add(linkTo(methodOn(UserController.class)
                    .getOneUser(user.getUserId())).withSelfRel()));
        }

        return status(OK).body(userModelPage);
    }

    @PreAuthorize("hasAnyRole('ROLE_STUDENT')")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getOneUser(@PathVariable(value = "userId") UUID userId){
        UUID currentUserId = authenticationCurrentUserService.getCurrentUser().getUserId();//Aula semana 7 ultimo bloco

        if(currentUserId.equals(userId)){
            Optional<UserModel> userModelOptional = existsUser(userId);
            if(userModelOptional.isEmpty()){
                return status(NOT_FOUND).body("User not found");
            } else {
                return status(OK).body(userModelOptional.get());
            }
        }else {
            throw new AccessDeniedException("Forbidden");
        }

    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable(value = "userId") UUID userId){
        log.debug("DELETE deleteUser userId received {}", userId);
        Optional<UserModel> userModelOptional = existsUser(userId);
        if(userModelOptional.isEmpty()) {
            return status(NOT_FOUND).body("User not found");
        } else {
            this.userService.deleteUser(userModelOptional.get());
            log.debug("DELETE User userId deleted {}", userId);
            log.info("DELETED successfully userId {}", userId);
            return status(OK).body("User deleted successful");
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable(value = "userId") UUID userId,
                                       @RequestBody @Validated(UserDto.UserView.UserPut.class)
                                       @JsonView(UserDto.UserView.UserPut.class) UserDto userDto){

        log.debug("PUT updateUser userDto received {}", userDto.toString());

        Optional<UserModel> userModelOptional = existsUser(userId);
        if(userModelOptional.isEmpty()) {
            return status(NOT_FOUND).body("User not found");
        } else {
            var userModel = userModelOptional.get();
            userModel.setFullName(userDto.getFullName());
            userModel.setPhoneNumber(userDto.getPhoneNumber());
            userModel.setCpf(userDto.getCpf());

            this.userService.updateUser(userModel);
            log.debug("POST UpdateUser userDto saved {}", userModel.getUserId());
            log.info("Updated successfully userId {}", userModel.getUserId());
            return status(OK).body(userModel);
        }
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<?> updatePassword(@PathVariable(value = "userId") UUID userId,
                                       @RequestBody @Validated(UserDto.UserView.PasswordPut.class)
                                       @JsonView(UserDto.UserView.PasswordPut.class) UserDto userDto){

        log.debug("PUT updatePassword userId received {}", userId);

        Optional<UserModel> userModelOptional = existsUser(userId);
        if(userModelOptional.isEmpty()) {
            return status(NOT_FOUND).body("User not found");
        } if(!userModelOptional.get().getPassword().equals(userDto.getOldPassword())){
            log.warn("Mismatched old password userId {} ", userId);
            return status(HttpStatus.CONFLICT).body("Error: Mismatched old password");
        } else {
            var userModel = userModelOptional.get();
            userModel.setPassword(userDto.getPassword());

            this.userService.updatePassword(userModel);
            log.debug("Password updated successfully userId {}", userId);
            log.info("Password updated successfully userId {}", userId);
            return status(OK).body("Password updated successfully");
        }
    }

    @PutMapping("/{userId}/image")
    public ResponseEntity<?> updateImage(@PathVariable(value = "userId") UUID userId,
                                         @RequestBody @Validated(UserDto.UserView.ImagePut.class)
                                         @JsonView(UserDto.UserView.ImagePut.class) UserDto userDto){

        log.debug("PUT updateImage userId received {}", userId);

        Optional<UserModel> userModelOptional = existsUser(userId);
        if(userModelOptional.isEmpty()) {
            return status(NOT_FOUND).body("User not found");
        } else {
            var userModel = userModelOptional.get();
            userModel.setImageUrl(userDto.getImageUrl());

            this.userService.updateUser(userModel);
            log.debug("Image updated successfully userId {}", userId);
            log.info("Image updated successfully userId {}", userId);
            return status(OK).body(userModel);
        }
    }

    private Optional<UserModel> existsUser(UUID userId){
        return this.userService.findById(userId);
    }
}
