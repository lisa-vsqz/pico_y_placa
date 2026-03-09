/**
 * Modelo de request para la consulta de Pico y Placa.
 */
export interface PicoYPlacaRequest {
  plateNumber: string;
  dateTime: string; // ISO 8601 format: yyyy-MM-ddTHH:mm:ss
}

/**
 * Modelo de response con el resultado de la consulta.
 */
export interface PicoYPlacaResponse {
  plateNumber: string;
  dateTime: string;
  canDrive: boolean;
  message: string;
}

/**
 * Modelo de error de la API.
 */
export interface ApiErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
  fieldErrors?: FieldError[];
}

/**
 * Detalle de error de validación de un campo.
 */
export interface FieldError {
  field: string;
  message: string;
}
