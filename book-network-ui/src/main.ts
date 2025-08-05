import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

// Bootstrap the application with the app config
bootstrapApplication(AppComponent, appConfig).catch(() => {});
