package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.status;

//@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/auth")
public class AuthenticationController {

    Logger log = LogManager.getLogger(AuthenticationController.class);//substituido pelo lombok

    //TODO Melhorar método registerUser() retirando os if's utilizando exceptions customizadas com o ExceptionHandler
    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Validated(UserDto.UserView.RegistrationPost.class)
                                              @JsonView(UserDto.UserView.RegistrationPost.class)
                                                      UserDto userDto){

        log.debug("POST registerUser userDto received {}", userDto.toString());

        if(userService.existsByUsername(userDto.getUsername())){
            log.warn("Username {} is already taken ", userDto.getUsername());
            return status(HttpStatus.CONFLICT).body("Error: Username is already taken!");
        }

        if(userService.existsByEmail(userDto.getEmail())){
            log.warn("Email {} is already taken ", userDto.getEmail());
            return status(HttpStatus.CONFLICT).body("Error: Email is already taken!");
        }

        var userModel = new UserModel();
        BeanUtils.copyProperties(userDto, userModel);
        userModel.setUserActived();
        userModel.setUserAsStudent();
        //userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC"))); //adicionado anotação no UserModel
        //userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        this.userService.saveUser(userModel);
        log.debug("RegisterUser UserId saved {}", userModel.getUserId());
        log.info("Saved successfully userId {}", userModel.getUserId());
        return status(HttpStatus.CREATED).body(userModel);
    }

    @GetMapping("/")
    public String index(){
        log.trace("TRACE");//trás mais detalhes
        log.debug("DEBUG");//utilizado em desenvolvimento
        log.info("INFO");//trás informações dos processos, geralmente com sucesso
        log.warn("WARN");//log que vai mostrar um alerta
        log.error("ERROR");//é quando algo dá errado - utilizar no try/catch
        return "Logging Spring Boot";
    }

}
