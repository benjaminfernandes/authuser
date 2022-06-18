package com.ead.authuser.controllers;

import com.ead.authuser.configs.security.JwtProvider;
import com.ead.authuser.dtos.JwtDto;
import com.ead.authuser.dtos.LoginDto;
import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.enums.RoleType;
import com.ead.authuser.models.RoleModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.RoleService;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    @Autowired
    private RoleService roleService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private AuthenticationManager authenticationManager;

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

        RoleModel roleModel = this.roleService.findByRoleName(RoleType.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));

        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

        var userModel = new UserModel();
        BeanUtils.copyProperties(userDto, userModel);
        userModel.setUserActived();
        userModel.setUserAsStudent();
        //userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC"))); //adicionado anotação no UserModel
        //userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.addRole(roleModel);
        this.userService.saveUser(userModel);
        log.debug("RegisterUser UserId saved {}", userModel.getUserId());
        log.info("Saved successfully userId {}", userModel.getUserId());
        return status(HttpStatus.CREATED).body(userModel);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtDto> authenticateUser(@Valid @RequestBody LoginDto loginDto){
        Authentication authentication = this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = this.jwtProvider.generateJwt(authentication);
        return ResponseEntity.ok(new JwtDto(jwt));
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
