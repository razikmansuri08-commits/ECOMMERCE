package com.rmtech.ecom.Repositories;

import com.rmtech.ecom.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface User_Repo extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Query("SELECT u FROM User u WHERE LOWER(u.name) = LOWER(:username)")
    User findbyusername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.name) = LOWER(:username)")
    boolean existsByUsernameIgnoreCase(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.id = :id")
    User findbyid(@Param("id") Long id);

    boolean existsByEmail(String email);

    Page<User> findAll(Pageable pageable);
}
