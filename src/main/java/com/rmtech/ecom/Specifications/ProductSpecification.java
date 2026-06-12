package com.rmtech.ecom.Specifications;

import com.rmtech.ecom.Entities.Category;
import com.rmtech.ecom.Entities.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {
    public static Specification<Product> hasCategory(
            Category category
    ) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("category").get("id"),
                        category.getId()
                );
    }
    public static Specification<Product> hasPriceGreaterThan(
            double price
    )
        {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(
                        root.get("price"),
                        price
                );
        }
    public static Specification<Product> hasPriceLessThan(
            double price
    )
        {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(
                        root.get("price"),
                        price
                );
        }
}
