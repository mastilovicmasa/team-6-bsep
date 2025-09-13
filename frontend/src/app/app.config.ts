import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient } from '@angular/common/http';
import { RECAPTCHA_SETTINGS, RecaptchaSettings } from 'ng-recaptcha';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(),
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    { provide: RECAPTCHA_SETTINGS, useValue: { siteKey: '6LebrccrAAAAAM5dsnueasV0Z8qjmzbURCSt6I8z' } as RecaptchaSettings }
  ]
};
