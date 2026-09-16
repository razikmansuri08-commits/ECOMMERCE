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

    // Derived exists queries (no @Query): Spring Data implements these as
    // SELECT 1 ... LIMIT 1 and never returns null, so the JDK proxy never
    // hits "Null return value from advice does not match primitive return type".
    @Query("SELECT 1 FROM User u WHERE LOWER(u.name) = LOWER(:username)")
    boolean existsByUsername(String username);

    @Query("SELECT 1 FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id = :id")
    User findbyid(@Param("id") Long id);

//    boolean existsByEmail(String email);

    Page<User> findAll(Pageable pageable);
}
