import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import 'jasmine';

import { ManageBookComponent } from './manage-book.component';
import { BookService } from '../../../../services/services/book.service';
import { CategoryService } from '../../../../services/services/category.service';
import { LocationService } from '../../../../services/location.service';
import { of } from 'rxjs';

declare const jasmine: any;

describe('ManageBookComponent', () => {
  let component: ManageBookComponent;
  let fixture: ComponentFixture<ManageBookComponent>;

  beforeEach(async () => {
    const bookService = jasmine.createSpyObj('BookService', ['saveBook']);
    const categoryService = jasmine.createSpyObj('CategoryService', ['findAll']);
    const locationService = jasmine.createSpyObj('LocationService', ['updateLocation', 'formatLocation']);

    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        ReactiveFormsModule,
        HttpClientTestingModule,
        ManageBookComponent
      ],
      providers: [
        { provide: BookService, useValue: bookService },
        { provide: CategoryService, useValue: categoryService },
        { provide: LocationService, useValue: locationService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManageBookComponent);
    component = fixture.componentInstance;
    
    // Mock category service response
    categoryService.findAll.and.returnValue(of([]));
    
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
