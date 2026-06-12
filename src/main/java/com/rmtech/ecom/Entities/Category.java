package com.rmtech.ecom.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Category implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id",nullable = true)
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory",fetch = FetchType.EAGER)
    private List<Category> subCategories =
            new ArrayList<>();

    @OneToMany(mappedBy = "category",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Product> products;
}

