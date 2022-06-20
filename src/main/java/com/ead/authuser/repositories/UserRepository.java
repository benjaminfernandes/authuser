package com.ead.authuser.repositories;

import com.ead.authuser.models.UserModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserModel, UUID>, JpaSpecificationExecutor<UserModel> {

    public boolean existsByUsername(String username);
    public boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "roles", type = EntityGraph.EntityGraphType.FETCH)//realiza o fetch de roles na consulta
    Optional<UserModel> findByUsername(String username);
    @EntityGraph(attributePaths = "roles", type = EntityGraph.EntityGraphType.FETCH)//realiza o fetch de roles na consulta
    Optional<UserModel> findById(UUID uuid);

}
