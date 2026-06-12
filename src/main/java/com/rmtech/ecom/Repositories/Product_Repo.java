package com.rmtech.ecom.Repositories;

import com.rmtech.ecom.Entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Product_Repo extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {

//    Page<Product> findAll(spec ,Pageable pageable);

//    @Query("SELECT p FROM Product p left join fetch p.category where p.id=:id")
//    Optional<Product> findbyidwithcategory(Long id);
}
