import { Component, OnInit } from '@angular/core';
import { BookRequest } from '../../../../services/models/book-request';
import { BookService } from '../../../../services/services/book.service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService, Category } from '../../../../services/services/category.service';

@Component({
  selector: 'app-manage-book',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './manage-book.component.html',
  styleUrl: './manage-book.component.scss'
})
export class ManageBookComponent implements OnInit {

  // 🖼️ Images & Vidéo
  selectedImages: File[] = [];
  selectedVideo: File | null = null;

  previewImages: string[] = [];
  previewVideo: string | null = null;

  // 📘 Formulaire
  errorMsg: Array<string> = [];
  bookRequest: BookRequest = {
    authorName: '',
    isbn: '',
    synopsis: '',
    title: ''
  };

  categories: Category[] = [];

  constructor(
    private bookService: BookService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.categoryService.getAllCategories().subscribe(categories => {
      this.categories = categories;
    });

    const bookId = this.activatedRoute.snapshot.params['bookId'];
    if (bookId) {
      this.bookService.findBookById({ 'book-id': bookId }).subscribe({
        next: (book) => {
          this.bookRequest = {
            id: book.id,
            title: book.title as string,
            authorName: book.authorName as string,
            isbn: book.isbn as string,
            synopsis: book.synopsis as string,
            shareable: book.shareable,
            categoryId: book.categoryId
          };

          // 🖼️ Prévisualisation unique si modification (ex: 1ère image)
          if (book.cover) {
            this.previewImages = ['data:image/jpg;base64,' + book.cover];
          }
        }
      });
    }
  }

  // 📤 Sauvegarder produit avec images/vidéo
  saveBook() {
    const formData = new FormData();

    // Champs texte
    formData.append('title', this.bookRequest.title);
    formData.append('authorName', this.bookRequest.authorName);
    formData.append('isbn', this.bookRequest.isbn);
    formData.append('synopsis', this.bookRequest.synopsis);
    formData.append('shareable', this.bookRequest.shareable ? 'true' : 'false');
    formData.append('categoryId', this.bookRequest.categoryId?.toString() || '');

    // Images (max 3)
    this.selectedImages.forEach((img) => {
      formData.append('images', img);
    });

    // Vidéo (optionnelle)
    if (this.selectedVideo) {
      formData.append('video', this.selectedVideo);
    }

    this.bookService.uploadBookWithMedia(formData).subscribe({
      next: () => {
        this.router.navigate(['/products/my-products']);
      },
      error: (err) => {
        this.errorMsg = err.error?.validationErrors || ['Erreur inattendue.'];
      }
    });
  }

  // 📸 Images multiples
  onMultipleImagesSelected(event: Event) {
    const files = (event.target as HTMLInputElement).files;
    if (files) {
      const imageFiles = Array.from(files);
      if (imageFiles.length > 3) {
        alert("Vous pouvez uploader jusqu'à 3 images maximum.");
        return;
      }

      this.selectedImages = imageFiles;
      this.previewImages = [];

      imageFiles.forEach(file => {
        const reader = new FileReader();
        reader.onload = () => {
          this.previewImages.push(reader.result as string);
        };
        reader.readAsDataURL(file);
      });
    }
  }

  // 🎥 Vidéo unique
  onVideoSelected(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.selectedVideo = file;

      const reader = new FileReader();
      reader.onload = () => {
        this.previewVideo = reader.result as string;
      };
      reader.readAsDataURL(file);
    }
  }
}
