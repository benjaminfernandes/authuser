package com.ead.authuser.repositories;

import com.ead.authuser.models.UserCourseModel;
import com.ead.authuser.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

public interface UserCourseRepository extends JpaRepository<UserCourseModel, UUID> {

    boolean existsByUserAndCourseId(UserModel userModel, UUID courseId);
    boolean existsByCourseId(UUID courseId);
    @Query(value = "SELECT * FROM TB_USERS_COURSES WHERE USER_ID = :userId", nativeQuery = true)
    List<UserCourseModel> findAllUserCourseIntoUser(@PathVariable("userId") UUID userId);
    void deleteAllByCourseId(UUID courseId);
}
