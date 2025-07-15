package com.aliboo.book.book;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    existing.setName(category.getName());
                    return ResponseEntity.ok(categoryRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 