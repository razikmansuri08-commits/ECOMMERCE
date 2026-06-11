package com.rmtech.ecom.unittests;

import com.rmtech.ecom.DTOS.ProductPageResponse;
import com.rmtech.ecom.DTOS.ProductRequestDto;
import com.rmtech.ecom.DTOS.ProductUpdate_Dto;
import com.rmtech.ecom.DTOS.Product_dto;
import com.rmtech.ecom.Entities.Category;
import com.rmtech.ecom.Entities.Product;
import com.rmtech.ecom.Exception.ProductNotFoundException;
import com.rmtech.ecom.Repositories.Category_Repo;
import com.rmtech.ecom.Repositories.Product_Repo;
import com.rmtech.ecom.Service.Product_Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class ProductServiceTest
{
    @Mock
    private Category_Repo category_Repo;

    @InjectMocks
    private Category category;

    @Mock
    private Product_Repo product_Repo;

    @InjectMocks
    private Product_Service productService;

    @Test
    void shouldrReturnProductIfExists() {
        Category category = new Category();
        category.setId(2L);
        category.setName("electronics");


        Product product = new Product();
        product.setId(1L);
        product.setName("phone");
        product.setCategory(category);
        product.setPrice(100.0);

        when(product_Repo.findById(1L))
                .thenReturn(Optional.of(product));
        Product_dto result = productService.get_prod(1L);

        assertEquals(1L, result.getId());
        assertEquals("phone", result.getName());
        assertEquals(100.0, result.getPrice());
        assertEquals("electronics", result.getCategory());

        verify(product_Repo).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        when(product_Repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.get_prod(1L));
        verify(product_Repo).findById(1L);
    }

    @Test
    void shouldCreateProductAndReturnDto()
    {
        Category category = new Category();
        category.setId(2L);
        category.setName("electronics");


        ProductRequestDto productDto = new ProductRequestDto();
        productDto.setName("phone");
        productDto.setPrice(100.0);
        productDto.setCategoryid(2L);


        when(category_Repo.findById(2L)).thenReturn(Optional.of(category));
        when(product_Repo.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Product_dto result = productService.create_prod(productDto);
        assertEquals("phone", result.getName());
        assertEquals(100.0, result.getPrice());
        assertEquals("electronics", result.getCategory());
        verify(category_Repo,times(1)).findById(any(Long.class));
        verify(product_Repo,times(1)).save(any(Product.class));
    }

    @Test
    void shouldDeleteProductIfExists()
    {
        Category category = new Category();
        category.setId(2L);
        category.setName("electronics");


        Product product = new Product();
        product.setId(1L);
        product.setName("phone");
        product.setCategory(category);
        product.setPrice(100.0);

        when(product_Repo.existsById(1L)).thenReturn(true);
        productService.delete_prod(1L);
        verify(product_Repo).deleteById(1L);
    }

    @Test
    void shouldUpdateProductAndReturnDto()
    {
        Category category = new Category();
        category.setId(2L);
        category.setName("electronics");

        Product product = new Product();
        product.setId(1L);
        product.setName("phone");
        product.setCategory(category);
        product.setPrice(100.0);

        ProductUpdate_Dto productUpdateDto=new ProductUpdate_Dto();
        productUpdateDto.setName("smartphone");
        productUpdateDto.setPrice(200.0);

        when(product_Repo.findById(1L)).thenReturn(Optional.of(product));
        when(product_Repo.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Product_dto result = productService.update_prod(1L, productUpdateDto);

        assertEquals("smartphone", result.getName());
        assertEquals(200.0, result.getPrice());

        verify(product_Repo).findById(1L);
        verify(product_Repo).save(any(Product.class));

    }

    @Test
    void shouldReturnParentCategorizedProducts()
    {
        Category category = new Category();
        category.setId(2L);
        category.setName("electronics");
        category.setParentCategory(null);

        when(category_Repo.findById(2L)).thenReturn(Optional.of(category));
        Product product = new Product();
        product.setId(1L);
        product.setName("phone");
        product.setCategory(category);
        product.setPrice(100.0);

        Page<Product> page=new PageImpl<>(List.of(product));
        when(product_Repo.findAll(any(Specification.class),any(Pageable.class))).thenReturn(page);
        ProductPageResponse result=productService.getparentcategorizedProducts(2L, PageRequest.of(0,5));
        assertEquals(
                1,
                result.getProducts().size()
        );
        verify(category_Repo).findById(2L);
        verify(product_Repo).findAll(any(Specification.class),any(Pageable.class));
    }

    @Test
    void shouldReturnExpensiveProducts()
    {
        Category category = new Category();
        category.setId(2L);
        category.setName("electronics");
        category.setParentCategory(null);

        when(category_Repo.findById(2L)).thenReturn(Optional.of(category));
        Product product = new Product();
        product.setId(1L);
        product.setName("phone");
        product.setCategory(category);
        product.setPrice(100.0);

        Page<Product> page=new PageImpl<>(List.of(product));
        when(product_Repo.findAll(any(Specification.class),any(Pageable.class))).thenReturn(page);
        ProductPageResponse result=productService.getexpensiveProducts(2L,50, PageRequest.of(0,5));
        assertEquals(
                1,
                result.getProducts().size()
        );
        verify(category_Repo).findById(2L);
        verify(product_Repo).findAll(any(Specification.class),any(Pageable.class));
    }

    @Test
    void shouldReturnCheapProducts()
    {
        Category category = new Category();
        category.setId(2L);
        category.setName("electronics");
        category.setParentCategory(null);

        when(category_Repo.findById(2L)).thenReturn(Optional.of(category));
        Product product = new Product();
        product.setId(1L);
        product.setName("phone");
        product.setCategory(category);
        product.setPrice(100.0);

        Page<Product> page=new PageImpl<>(List.of(product));
        when(product_Repo.findAll(any(Specification.class),any(Pageable.class))).thenReturn(page);
        ProductPageResponse result=productService.getcheapProducts(2L,100, PageRequest.of(0,5));
        assertEquals(
                1,
                result.getProducts().size()
        );
        verify(category_Repo).findById(2L);
        verify(product_Repo).findAll(any(Specification.class),any(Pageable.class));
    }

    @Test
    void shouldReturnAllProducts()
    {
        Category category = new Category();
        category.setId(2L);
        category.setName("electronics");
        category.setParentCategory(null);

        Product product = new Product();
        product.setId(1L);
        product.setName("phone");
        product.setCategory(category);
        product.setPrice(100.0);

        Page<Product> page=new PageImpl<>(List.of(product));
        when(product_Repo.findAll(any(Pageable.class))).thenReturn(page);
        ProductPageResponse result=productService.getallProducts(PageRequest.of(0,5));
        assertEquals(
                1,
                result.getProducts().size()
        );
        verify(product_Repo).findAll(any(Pageable.class));


    }
}
