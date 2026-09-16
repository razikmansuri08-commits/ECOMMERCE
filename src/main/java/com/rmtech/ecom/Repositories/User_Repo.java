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

    // ------------------------------------------------------------------
    // Externalised existence checks.
    //
    // Why @Query + Boolean wrapper instead of derived `boolean existsBy...`?
    // 1. The entity field is `name`, not `username`, so a derived
    //    `existsByUsername...` cannot be resolved (no property `username`
    //    on User) and fails bootstrap with "No property username found".
    //    An explicit JPQL query decouples the method name from the field.
    // 2. `SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END` always
    //    returns non-null TRUE/FALSE (never null), and the `Boolean`
    //    wrapper means even if an AOP advice (cache/tx) returns null the
    //    JDK proxy will not throw:
    //    "Null return value from advice does not match primitive return
    //    type". Callers must use Boolean.TRUE.equals(...) to null-guard.
    // ------------------------------------------------------------------

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.email = :email")
    Boolean existsByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.name = :username")
    Boolean existsByUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Boolean existsByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE LOWER(u.name) = LOWER(:username)")
    Boolean existsByUsernameIgnoreCase(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.id = :id")
    User findbyid(@Param("id") Long id);

    Page<User> findAll(Pageable pageable);
}
