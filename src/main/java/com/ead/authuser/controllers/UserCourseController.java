package com.ead.authuser.controllers;

import com.ead.authuser.clients.CourseClient;
import com.ead.authuser.dtos.CourseDto;
import com.ead.authuser.dtos.UserCourseDto;
import com.ead.authuser.models.UserCourseModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserCourseService;
import com.ead.authuser.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.status;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge =  3600) //pode ser colocado globalmente atraves de config. Tem no ESR Algaworks
public class UserCourseController {

    @Autowired
    private CourseClient userClient;

    @Autowired
    private UserService userService;

    @Autowired
    private UserCourseService userCourseService;

    @GetMapping("/users/{userId}/courses")
    public ResponseEntity<Page<CourseDto>> getAllCoursesByUser(@PageableDefault(page = 0, size = 10, sort = "courseId",
                                                        direction = Sort.Direction.ASC) Pageable pageable,
                                                               @PathVariable(value = "userId") UUID userId){

        return ResponseEntity.status(HttpStatus.OK).body(userClient.getAllCoursesByUser(userId, pageable));
    }

    @PostMapping("/users/{userId}/courses/subscription")
    public ResponseEntity<?> saveSubscriptionUserInCourse(@PathVariable(value = "userId") UUID userId,
                                                          @RequestBody @Valid UserCourseDto userCourseDto){
        Optional<UserModel> userModelOptional = existsUser(userId);
        if(!userModelOptional.isPresent()){
            return status(NOT_FOUND).body("User not found");
        }

        if(this.userCourseService.existsByUserAndCourseId(userModelOptional.get(), userCourseDto)){
            return status(CONFLICT).body("Error: subscription already exists!");
        }

        UserCourseModel userCourseModel = this.userCourseService.save(userModelOptional.get()
                .convertToUserCourseModel(userCourseDto.getCourseId()));

        return status(CREATED).body(userCourseModel);
    }

    private Optional<UserModel> existsUser(UUID userId){
        return this.userService.findById(userId);
    }
}
