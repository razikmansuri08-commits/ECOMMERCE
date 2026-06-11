package com.rmtech.ecom.Repositories;

import com.rmtech.ecom.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


public interface User_Repo extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {

    @Query("SELECT u FROM User u WHERE u.name=:username")
    User findbyusername(String username);

    @Query("SELECT u FROM User u WHERE u.id=:id")
    User findbyid(Long id);
    boolean existsByEmail(String email);

    Page<User> findAll(Pageable pageable);

    boolean existsByname(String username);

}
