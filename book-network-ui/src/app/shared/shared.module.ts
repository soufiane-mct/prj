import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MapPickerComponent } from './components/map-picker/map-picker.component';
import { FooterComponent } from './footer/footer.component';


@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    MapPickerComponent, // Import standalone component
    FooterComponent
  ],
  exports: [
    MapPickerComponent,
    FooterComponent
  ]
})
export class SharedModule { }
