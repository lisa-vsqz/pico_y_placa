import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
} from "@angular/forms";
import { PicoYPlacaService } from "../../services/pico-y-placa.service";
import { PicoYPlacaResponse } from "../../models/pico-y-placa.model";

/**
 * Componente principal del formulario de consulta de Pico y Placa.
 *
 * Responsabilidades:
 * - Capturar y validar datos del formulario
 * - Delegar la consulta al servicio
 * - Mostrar resultado o errores al usuario
 *
 * Validaciones del lado del cliente (complementan al backend):
 * - Formato de placa: 3 letras + guión opcional + 3 o 4 dígitos
 * - Fecha no vacía
 * - Fecha no en el pasado
 */
@Component({
  selector: "app-pico-y-placa-form",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: "./pico-y-placa-form.component.html",
  styleUrls: ["./pico-y-placa-form.component.css"],
})
export class PicoYPlacaFormComponent implements OnInit {
  form!: FormGroup;
  result: PicoYPlacaResponse | null = null;
  errorMessage: string | null = null;
  isLoading = false;
  minDateTime = "";

  constructor(
    private fb: FormBuilder,
    private picoYPlacaService: PicoYPlacaService,
  ) {}

  ngOnInit(): void {
    this.setMinDateTime();
    this.initForm();
  }

  /**
   * Inicializa el formulario reactivo con validaciones.
   */
  private initForm(): void {
    this.form = this.fb.group({
      plateNumber: [
        "",
        [Validators.required, Validators.pattern(/^[A-Za-z]{3}-?\d{3,4}$/)],
      ],
      dateTime: [
        "",
        [Validators.required, this.dateNotInPastValidator.bind(this)],
      ],
    });
  }

  /**
   * Establece la fecha/hora mínima permitida (ahora).
   */
  private setMinDateTime(): void {
    const now = new Date();
    // Ajustar a formato datetime-local: yyyy-MM-ddTHH:mm
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    const hours = String(now.getHours()).padStart(2, "0");
    const minutes = String(now.getMinutes()).padStart(2, "0");
    this.minDateTime = `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  /**
   * Validador personalizado: verifica que la fecha no sea anterior a la actual.
   */
  dateNotInPastValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // La validación de requerido se encarga de esto
    }
    const selectedDate = new Date(control.value);
    const now = new Date();
    // Dar un margen de 1 minuto para evitar problemas de latencia
    now.setMinutes(now.getMinutes() - 1);
    if (selectedDate < now) {
      return { dateInPast: true };
    }
    return null;
  }

  /**
   * Envía la consulta al backend.
   */
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // Reset estado
    this.result = null;
    this.errorMessage = null;
    this.isLoading = true;

    // Actualizar validación de fecha (puede haber cambiado)
    this.setMinDateTime();

    const plateNumber = this.form.value.plateNumber.toUpperCase().trim();
    const dateTimeValue = this.form.value.dateTime;
    // El input datetime-local devuelve 'yyyy-MM-ddTHH:mm', agregar segundos
    const dateTime =
      dateTimeValue.length === 16 ? dateTimeValue + ":00" : dateTimeValue;

    this.picoYPlacaService
      .checkRestriction({ plateNumber, dateTime })
      .subscribe({
        next: (response) => {
          this.result = response;
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage =
            err.message || "Ha ocurrido un error. Intente nuevamente.";
          this.isLoading = false;
        },
      });
  }

  /**
   * Limpia el formulario y los resultados.
   */
  onReset(): void {
    this.form.reset();
    this.result = null;
    this.errorMessage = null;
    this.setMinDateTime();
  }

  /**
   * Formatea la placa mientras el usuario escribe (uppercase y guión automático).
   */
  onPlateInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.toUpperCase().replace(/[^A-Z0-9-]/g, "");

    // Auto-insertar guión después de 3 letras si no existe
    if (
      value.length === 4 &&
      !value.includes("-") &&
      /^[A-Z]{3}\d$/.test(value)
    ) {
      value = value.substring(0, 3) + "-" + value.substring(3);
    }

    // Limitar longitud máxima
    if (value.length > 8) {
      value = value.substring(0, 8);
    }

    input.value = value;
    this.form.get("plateNumber")?.setValue(value, { emitEvent: false });
  }

  /** Helpers para acceder a controles del formulario en el template */
  get plateControl() {
    return this.form.get("plateNumber");
  }
  get dateTimeControl() {
    return this.form.get("dateTime");
  }
}
