import { Component } from "@angular/core";
import { PicoYPlacaFormComponent } from "./components/pico-y-placa-form/pico-y-placa-form.component";

/**
 * Componente raíz de la aplicación.
 * Estructura simple: header + contenido principal + footer.
 */
@Component({
  selector: "app-root",
  standalone: true,
  imports: [PicoYPlacaFormComponent],
  template: `
    <div class="app-container">
      <header class="app-header">
        <div class="header-content">
          <div class="logo">
            <h1>Pico y Placa</h1>
          </div>
          <p class="subtitle">
            Consulta de restricción vehicular — Quito, Ecuador
          </p>
        </div>
      </header>

      <main class="app-main">
        <app-pico-y-placa-form />
      </main>

      <footer class="app-footer">
        <p>
          Sistema de consulta de Pico y Placa · Las reglas pueden cambiar según
          la normativa vigente
        </p>
      </footer>
    </div>
  `,
  styles: [
    `
      .app-container {
        display: flex;
        flex-direction: column;
        min-height: 100vh;
      }

      .app-header {
        background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%);
        color: white;
        padding: 2rem 1rem;
        text-align: center;
        box-shadow: var(--shadow-md);
      }

      .header-content {
        max-width: 800px;
        margin: 0 auto;
      }

      .logo {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.75rem;
        margin-bottom: 0.5rem;
      }

      .logo-icon {
        font-size: 2rem;
      }

      .logo h1 {
        font-size: 1.75rem;
        font-weight: 700;
        letter-spacing: -0.02em;
      }

      .subtitle {
        font-size: 0.95rem;
        opacity: 0.85;
        font-weight: 300;
      }

      .app-main {
        flex: 1;
        padding: 2rem 1rem;
        max-width: 640px;
        margin: 0 auto;
        width: 100%;
      }

      .app-footer {
        text-align: center;
        padding: 1.5rem 1rem;
        color: var(--text-secondary);
        font-size: 0.8rem;
        border-top: 1px solid var(--border-color);
        background: white;
      }

      @media (max-width: 480px) {
        .app-header {
          padding: 1.5rem 1rem;
        }
        .logo h1 {
          font-size: 1.4rem;
        }
        .app-main {
          padding: 1rem 0.75rem;
        }
      }
    `,
  ],
})
export class AppComponent {}
