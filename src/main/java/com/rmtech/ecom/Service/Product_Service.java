package com.rmtech.ecom.Service;

import com.rmtech.ecom.DTOS.ProductPageResponse;
import com.rmtech.ecom.DTOS.ProductRequestDto;
import com.rmtech.ecom.DTOS.ProductUpdate_Dto;
import com.rmtech.ecom.DTOS.Product_dto;
import com.rmtech.ecom.Entities.Category;
import com.rmtech.ecom.Entities.Product;
import com.rmtech.ecom.Exception.MethodArgumentInvalid;
import com.rmtech.ecom.Exception.ProductNotFoundException;
import com.rmtech.ecom.Repositories.Category_Repo;
import com.rmtech.ecom.Repositories.Product_Repo;
import com.rmtech.ecom.Specifications.ProductSpecification;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class Product_Service
{
    private final Product_Repo prr;
    private final Category_Repo crr;

    public Product_Service(Product_Repo prr, Category_Repo crr) {
        this.prr = prr;
        this.crr = crr;
    }


    public boolean find_prod(Long id)
    {
        return prr.existsById(id);
    }

    @Transactional
    public Product_dto create_prod(ProductRequestDto prd){

        if(prd.getName()==null)
            throw new MethodArgumentInvalid("Product name is required");
        if(prd.getPrice()<=0)
            throw new MethodArgumentInvalid("Product price must be greater than 0");

        Category category=crr.findById(prd.getCategoryid()).orElseThrow(()->new ProductNotFoundException("Category not found"));
        Product product = new Product();
        product.setCategory(category);
        product.setName(prd.getName());
        product.setPrice(prd.getPrice());
        Product pr=prr.save(product);
        return convertToDTO(pr);

    }

    @Cacheable( key = "#id",value = "products")
    public Product_dto get_prod(Long id)
    {
        log.info("Fetching from DB");
        Product product=prr.findById(id).orElseThrow(()->new ProductNotFoundException("Product not found"));
        return convertToDTO(product);
    }

    @Transactional
    @CachePut(key="#id",value = "products")
    public Product_dto update_prod(Long id, ProductUpdate_Dto prod)
    {
        Product existing_prod = prr.findById(id).orElseThrow(()->new ProductNotFoundException("Product not found"));
        Product_dto productDto=new Product_dto();
        existing_prod.setName(prod.getName());
        productDto.setName(prod.getName());
        existing_prod.setPrice(prod.getPrice());
        productDto.setPrice(prod.getPrice());
        Product product=prr.save(existing_prod);
        return productDto;
    }

    @Transactional
    @CacheEvict(key="#id",value = "products")
    public void delete_prod(Long id)
    {
        if(!prr.existsById(id))
            throw new ProductNotFoundException("Product not found");
        prr.deleteById(id);
    }

    @Cacheable(key = "#pageable.pageNumber + '_' + #pageable.pageSize",value = "products")
    @Transactional
    public ProductPageResponse getallProducts(Pageable pageable)
    {

        Page<Product> productPage = prr.findAll(pageable);
        List<Product_dto> productdtos = productPage.getContent()
                .stream().map(this::convertToDTO).toList();
        ProductPageResponse response=new ProductPageResponse();
        response.setProducts(productdtos);
        response.setCurrentPage(productPage.getNumber());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalItems(productPage.getTotalElements());
        return response;
    }


    @Transactional
    public ProductPageResponse getexpensiveProducts(Long categoryid,double minPrice ,Pageable pageable)
    {
        Category category = crr.findById(categoryid).orElseThrow(()->new ProductNotFoundException("Category not found"));


        Specification<Product> spec =
                Specification.where(null);

        if(category != null) {

            spec = spec.and(
                    ProductSpecification
                            .hasCategory(category)
            );
        }
        spec = spec.and(
                ProductSpecification
                        .hasPriceGreaterThan(minPrice)
        );

        Page<Product> productPage = prr.findAll(spec,pageable);
        List<Product_dto> productdtos = productPage.getContent()
                .stream().map(this::convertToDTO).toList();
        ProductPageResponse response=new ProductPageResponse();
        response.setProducts(productdtos);
        response.setCurrentPage(productPage.getNumber());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalItems(productPage.getTotalElements());
        return response;
    }

    @Transactional
    public ProductPageResponse getparentcategorizedProducts(Long parentcategoryid,Pageable pageable)
    {

        Specification<Product> spec =
                Specification.where(null);

        Category category = crr.findById(parentcategoryid).orElseThrow(()->new ProductNotFoundException("Category not found"));
        if(category.getParentCategory()== null) {

            spec = spec.and(
                    ProductSpecification
                            .hasCategory(category)
            );

            Page<Product> productPage = prr.findAll(spec,pageable);
            List<Product_dto> productdtos = productPage.getContent()
                    .stream().map(this::convertToDTO).toList();
            ProductPageResponse response=new ProductPageResponse();
            response.setProducts(productdtos);
            response.setCurrentPage(productPage.getNumber());
            response.setTotalPages(productPage.getTotalPages());
            response.setTotalItems(productPage.getTotalElements());
            return response;
        }
        else
        {
            throw new ProductNotFoundException("Product not found");
        }
    }

    @Transactional
    public ProductPageResponse getcheapProducts(Long categoryid,double maxPrice ,Pageable pageable)
    {

        Category category=crr.findById(categoryid).orElseThrow(()->new ProductNotFoundException("Category not found"));
        Specification<Product> spec =
                Specification.where(null);

        if(category != null) {

            spec = spec.and(
                    ProductSpecification
                            .hasCategory(category)
            );
        }
        spec = spec.and(
                ProductSpecification
                        .hasPriceLessThan(maxPrice)
        );

        Page<Product> productPage = prr.findAll(spec,pageable);
        List<Product_dto> productdtos = productPage.getContent()
                .stream().map(this::convertToDTO).toList();
        ProductPageResponse response=new ProductPageResponse();
        response.setProducts(productdtos);
        response.setCurrentPage(productPage.getNumber());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalItems(productPage.getTotalElements());
        return response;
    }


    private Product_dto convertToDTO(Product product) {
        Product_dto productDto = new Product_dto();
        productDto.setId(product.getId());

        productDto.setName(product.getName());
        if(product.getCategory().getParentCategory()!=null) {
            productDto.setParentcategory((product.getCategory().getParentCategory().getName()));
        }
        productDto.setCategory(product.getCategory().getName());
        productDto.setPrice(product.getPrice());
        return productDto;
    }

}

