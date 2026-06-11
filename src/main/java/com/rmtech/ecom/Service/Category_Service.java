package com.rmtech.ecom.Service;

import com.rmtech.ecom.DTOS.CategoryDto;
import com.rmtech.ecom.DTOS.CategoryPageResponse;
import com.rmtech.ecom.DTOS.CategoryTreeDto;
import com.rmtech.ecom.Entities.Category;
import com.rmtech.ecom.Repositories.Category_Repo;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Category_Service {
    private final Category_Repo category_Repo;
    public Category_Service(Category_Repo category_Repo) {
        this.category_Repo = category_Repo;
    }

    @Transactional
    @Cacheable(value = "categories", key = "#id")
    @CacheEvict(
            value = "categories",
            allEntries = true
    )
    public CategoryDto createCategory(CategoryDto cat)
    {
        if (category_Repo.existsByName(cat.getName())) {
            throw new IllegalArgumentException("Category name already exists");
        }
        Category category =
                new Category();

        category.setName(cat.getName());

        if(cat.getParent_id()
                != null) {

            Category parent =
                    category_Repo.findById(
                            cat.getParent_id()
                    ).orElseThrow();

            category.setParentCategory(parent);
            parent.getSubCategories().add(category);
        }
        else {
            category.setParentCategory(null);
        }
        return convertToDTO(category_Repo.save(category));
    }

    private CategoryDto convertToDTO(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        if(category.getParentCategory() != null) {
            categoryDto.setParent_id(category.getParentCategory().getId());
        }
        return categoryDto;
    }
    @Cacheable
(value = "categories", key = "#pageable.pageNumber + '_' + #pageable.pageSize +'_' + #pageable.sort")
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryPageResponse getallcategories(Pageable pageable)
    {
        Page<Category> categoryPage = category_Repo.findAll(pageable);
        List<CategoryDto> categoryDtos = categoryPage.getContent()
                .stream().map((this::convertToDTO)).toList();
        CategoryPageResponse categoryPageresponse=new CategoryPageResponse();
        categoryPageresponse.setCategoryDtos(categoryDtos);
        categoryPageresponse.setCurrentPage(categoryPage.getNumber());
        categoryPageresponse.setTotalPages(categoryPage.getTotalPages());
        categoryPageresponse.setTotalItems(categoryPage.getTotalElements());
        return categoryPageresponse;
    }


    @Cacheable(value = "ParentCategory", key = "#id")
    @CacheEvict(value = "ParentCategory", allEntries = true)
    public CategoryTreeDto getcat_tree(Long id)
    { Category category = category_Repo.findCategorywithchildren(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));

        return getcategorytree(category);
    }

    public CategoryTreeDto getcategorytree(Category category)
    {
        CategoryTreeDto categoryTreeDto=new CategoryTreeDto();
        categoryTreeDto.setId(category.getId());

        categoryTreeDto.setName(category.getName());

        List<CategoryTreeDto> children =
                category.getSubCategories()
                        .stream()
                        .map(this::getcategorytree)
                        .toList();
        categoryTreeDto.setChildren(children);
        return categoryTreeDto;
    }


}
