import { ApplicationConfig, provideZoneChangeDetection } from "@angular/core";
import { provideHttpClient } from "@angular/common/http";

/**
 * Configuración raíz de la aplicación Angular.
 * Standalone app sin módulos — enfoque moderno Angular 18.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideHttpClient(),
  ],
};
