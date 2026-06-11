package com.rmtech.ecom.Repositories;

import com.rmtech.ecom.Entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface Category_Repo extends JpaRepository<Category,Long>, JpaSpecificationExecutor<Category> {

    List<Category> findByParentCategoryIsNull();

    @Query("SELECT DISTINCT c from Category c left join fetch c.subCategories where c.id=:id")
    Optional<Category> findCategorywithchildren(@Param("id") Long id);

    Page<Category> findByParentCategory(Category parentCategory, Pageable pageable);

    @Query("SELECT c.name FROM Category c")
    List<String> findAllCategoryNames();

    @Query("SELECT c FROM Category c WHERE c.name=:name")
    Category findByname(String name);

    boolean existsByName(String name);
}
