package com.ead.authuser.services;

import com.ead.authuser.dtos.UserCourseDto;
import com.ead.authuser.models.UserCourseModel;
import com.ead.authuser.models.UserModel;

public interface UserCourseService {
    boolean existsByUserAndCourseId(UserModel userModel, UserCourseDto userCourseDto);
    UserCourseModel save(UserCourseModel userCourseModel);
}
