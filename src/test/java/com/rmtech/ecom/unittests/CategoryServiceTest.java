package com.rmtech.ecom.unittests;

import com.rmtech.ecom.DTOS.*;
import com.rmtech.ecom.Entities.*;
import com.rmtech.ecom.Repositories.*;
import com.rmtech.ecom.Service.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private Category_Repo categoryRepo;
    @InjectMocks
    private Category_Service categoryService;
    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {

        when(categoryRepo.findCategorywithchildren(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> categoryService.getcat_tree(1L)
                );

        assertEquals(
                "Category not found",
                exception.getMessage()
        );

        verify(categoryRepo)
                .findCategorywithchildren(1L);
    }

    @Test
    void shouldBuildNestedCategoryTree() {

        Category root = new Category();
        root.setId(1L);
        root.setName("Electronics");

        Category mobiles = new Category();
        mobiles.setId(2L);
        mobiles.setName("Mobiles");

        Category android = new Category();
        android.setId(3L);
        android.setName("Android");

        Category laptops = new Category();
        laptops.setId(4L);
        laptops.setName("Laptops");

        android.setSubCategories(Collections.emptyList());

        mobiles.setSubCategories(List.of(android));

        laptops.setSubCategories(Collections.emptyList());

        root.setSubCategories(
                List.of(mobiles, laptops)
        );

        CategoryTreeDto dto =
                categoryService.getcategorytree(root);

        assertEquals("Electronics",
                dto.getName());

        assertEquals(2,
                dto.getChildren().size());

        assertEquals("Mobiles",
                dto.getChildren().get(0).getName());

        assertEquals("Android",
                dto.getChildren()
                        .getFirst()
                        .getChildren()
                        .getFirst()
                        .getName());

        assertEquals("Laptops",
                dto.getChildren().get(1).getName());
    }

    @Test
    void shouldReturnLeafCategoryTree() {

        Category category = new Category();
        category.setId(1L);
        category.setName("Mobiles");
        category.setSubCategories(
                Collections.emptyList()
        );

        CategoryTreeDto dto =
                categoryService.getcategorytree(category);

        assertEquals(1L, dto.getId());
        assertEquals("Mobiles", dto.getName());

        assertTrue(dto.getChildren().isEmpty());
    }
}
