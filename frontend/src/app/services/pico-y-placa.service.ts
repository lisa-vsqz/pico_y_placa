import { Injectable } from "@angular/core";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import {
  PicoYPlacaRequest,
  PicoYPlacaResponse,
  ApiErrorResponse,
} from "../models/pico-y-placa.model";
import { environment } from "../../environments/environment";

/**
 * Servicio para comunicación con la API de Pico y Placa.
 *
 * Responsabilidad única: manejar las llamadas HTTP al backend.
 * El manejo de errores transforma respuestas HTTP en errores legibles.
 */
@Injectable({
  providedIn: "root",
})
export class PicoYPlacaService {
  private readonly apiUrl = `${environment.apiUrl}/pico-y-placa`;

  constructor(private http: HttpClient) {}

  /**
   * Consulta la restricción de Pico y Placa para una placa y fecha/hora dadas.
   *
   * @param request datos de la consulta
   * @returns Observable con la respuesta del servidor
   */
  checkRestriction(request: PicoYPlacaRequest): Observable<PicoYPlacaResponse> {
    return this.http
      .post<PicoYPlacaResponse>(this.apiUrl, request)
      .pipe(catchError(this.handleError));
  }

  /**
   * Transforma errores HTTP en mensajes legibles para el usuario.
   * No expone detalles técnicos internos.
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let userMessage: string;

    if (error.status === 0) {
      // Error de red o servidor no disponible
      userMessage =
        "No se pudo conectar con el servidor. Verifique su conexión a internet e intente nuevamente.";
    } else if (error.error && typeof error.error === "object") {
      // Error estructurado de la API
      const apiError = error.error as ApiErrorResponse;

      if (apiError.fieldErrors && apiError.fieldErrors.length > 0) {
        userMessage = apiError.fieldErrors.map((fe) => fe.message).join(". ");
      } else {
        userMessage =
          apiError.message || "Ha ocurrido un error al procesar la consulta.";
      }
    } else {
      userMessage =
        "Ha ocurrido un error inesperado. Por favor, intente nuevamente.";
    }

    return throwError(() => ({ message: userMessage, status: error.status }));
  }
}
