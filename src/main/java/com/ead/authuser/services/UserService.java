package com.ead.authuser.services;

import com.ead.authuser.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    public List<UserModel> findAll();
    public Optional<UserModel> findById(UUID userId);
    public void delete(UserModel user);
    public UserModel save(UserModel userModel);
    public boolean existsByUsername(String username);
    public boolean  existsByEmail(String email);
    public Page<UserModel> findAll(Specification<UserModel> spec, Pageable pageable);
    public UserModel saveUser(UserModel userModel);
    public void deleteUser(UserModel userModel);
    public UserModel updateUser(UserModel userModel);
    public UserModel updatePassword(UserModel userModel);
}
