package com.aliboo.book.book;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByName(String name);
} 