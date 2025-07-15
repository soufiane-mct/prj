import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiConfiguration } from '../api-configuration';
import { BaseService } from '../base-service';

export interface Category {
  id: number;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class CategoryService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  getAllCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.rootUrl}/categories`);
  }
} 