import { Component, OnInit } from '@angular/core';
import { CategoryService, Category } from '../../../../services/services/category.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-category-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category-management.component.html',
  styleUrl: './category-management.component.scss'
})
export class CategoryManagementComponent implements OnInit {
  categories: Category[] = [];
  newCategoryName = '';
  editCategoryId: number | null = null;
  editCategoryName = '';
  errorMsg = '';

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories() {
    this.categoryService.getAllCategories().subscribe({
      next: (categories) => this.categories = categories,
      error: () => this.errorMsg = 'Failed to load categories.'
    });
  }

  addCategory() {
    if (!this.newCategoryName.trim()) return;
    this.categoryService['http'].post<Category>(`${this.categoryService.rootUrl}/categories`, { name: this.newCategoryName })
      .subscribe({
        next: () => {
          this.newCategoryName = '';
          this.loadCategories();
        },
        error: () => this.errorMsg = 'Failed to add category.'
      });
  }

  startEdit(category: Category) {
    this.editCategoryId = category.id;
    this.editCategoryName = category.name;
  }

  saveEdit() {
    if (this.editCategoryId == null || !this.editCategoryName.trim()) return;
    this.categoryService['http'].put<Category>(`${this.categoryService.rootUrl}/categories/${this.editCategoryId}`, { name: this.editCategoryName })
      .subscribe({
        next: () => {
          this.editCategoryId = null;
          this.editCategoryName = '';
          this.loadCategories();
        },
        error: () => this.errorMsg = 'Failed to update category.'
      });
  }

  cancelEdit() {
    this.editCategoryId = null;
    this.editCategoryName = '';
  }

  deleteCategory(id: number) {
    if (!confirm('Are you sure you want to delete this category?')) return;
    this.categoryService['http'].delete(`${this.categoryService.rootUrl}/categories/${id}`)
      .subscribe({
        next: () => this.loadCategories(),
        error: () => this.errorMsg = 'Failed to delete category.'
      });
  }
} 